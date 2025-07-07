import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File

/**
 * Gradle task that consolidates a JSON/YAML schema with external file references into a single file.
 *
 * This task takes a JSON or YAML schema that includes references to schemas in other files
 * and creates a single file version by copying externally referenced schemas under
 * a `definitions` element in the main file, then updating the references to refer
 * to the definitions section instead of external files.
 *
 * Supports both JSON and YAML input files, and can reference both JSON and YAML external files.
 * The output format (JSON or YAML) is determined by the output file extension:
 * - .yaml or .yml extensions will produce YAML output
 * - All other extensions will produce JSON output
 */
@CacheableTask
abstract class ConsolidateJsonSchemaTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    private val jsonMapper = ObjectMapper().registerKotlinModule()
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    @TaskAction
    fun consolidateSchema() {
        val input = inputFile.asFile.get()
        val output = outputFile.asFile.get()
        
        if (!input.exists()) {
            throw IllegalArgumentException("Input file does not exist: ${input.absolutePath}")
        }
        
        val consolidator = SchemaConsolidator(jsonMapper, yamlMapper)
        consolidator.consolidate(input, output)
        
        logger.info("Successfully consolidated schema from '${input.name}' to '${output.name}'")
    }

    private class SchemaConsolidator(
        private val jsonMapper: ObjectMapper,
        private val yamlMapper: ObjectMapper
    ) {
        private val processedFiles = mutableSetOf<String>()
        private val definitions = mutableMapOf<String, JsonNode>()

        fun consolidate(inputFile: File, outputFile: File) {
            val baseDir = inputFile.parentFile ?: File(".")
            val mainSchema = readSchemaFile(inputFile)
            
            // Process the main schema and collect all external references
            val consolidatedSchema = processSchema(mainSchema, baseDir)
            
            // Add definitions section if we found any external references
            if (definitions.isNotEmpty()) {
                (consolidatedSchema as ObjectNode).set<JsonNode>("definitions",
                    jsonMapper.valueToTree(definitions))
            }

            // Write the consolidated schema in the format determined by output file extension
            outputFile.parentFile?.mkdirs()
            writeSchemaFile(outputFile, consolidatedSchema)
        }
        
        private fun processSchema(schema: JsonNode, baseDir: File): JsonNode {
            return when {
                schema.isObject -> processObjectNode(schema as ObjectNode, baseDir)
                schema.isArray -> {
                    val arrayNode = jsonMapper.createArrayNode()
                    schema.forEach { arrayNode.add(processSchema(it, baseDir)) }
                    arrayNode
                }
                else -> schema
            }
        }
        
        private fun processObjectNode(objectNode: ObjectNode, baseDir: File): ObjectNode {
            val result = jsonMapper.createObjectNode()
            
            objectNode.fields().forEach { (key, value) ->
                when (key) {
                    "\$ref" -> {
                        val refValue = value.asText()
                        if (isExternalFileReference(refValue)) {
                            // This is an external file reference
                            val definitionName = extractDefinitionName(refValue)
                            loadExternalSchema(refValue, baseDir)
                            result.put("\$ref", "#/definitions/$definitionName")
                        } else {
                            // This is an internal reference, keep as-is
                            result.set<JsonNode>(key, value)
                        }
                    }
                    else -> {
                        // Recursively process other properties
                        result.set<JsonNode>(key, processSchema(value, baseDir))
                    }
                }
            }
            
            return result
        }
        
        private fun isExternalFileReference(ref: String): Boolean {
            // Check if this is a reference to an external file
            // External references don't start with # and typically end with file extensions
            return !ref.startsWith("#") && (ref.contains(".json") || ref.contains(".yaml") || ref.contains(".yml"))
        }
        
        private fun extractDefinitionName(ref: String): String {
            // Extract the schema name from the file reference
            // Examples:
            // "other-schema.json" -> "other-schema"
            // "schemas/user.json#/properties/name" -> "user"
            // "path/to/schema.yaml" -> "schema"
            
            val filePart = if (ref.contains("#")) {
                ref.substringBefore("#")
            } else {
                ref
            }
            
            return File(filePart).nameWithoutExtension
        }
        
        private fun loadExternalSchema(ref: String, baseDir: File) {
            val filePart = if (ref.contains("#")) {
                ref.substringBefore("#")
            } else {
                ref
            }
            
            val fragmentPart = if (ref.contains("#")) {
                ref.substringAfter("#")
            } else {
                null
            }
            
            val externalFile = File(baseDir, filePart)
            val canonicalPath = externalFile.canonicalPath
            
            if (processedFiles.contains(canonicalPath)) {
                return // Already processed this file
            }
            
            if (!externalFile.exists()) {
                throw IllegalArgumentException("Referenced file does not exist: ${externalFile.absolutePath}")
            }
            
            processedFiles.add(canonicalPath)

            val externalSchema = readSchemaFile(externalFile)
            val schemaToAdd = if (fragmentPart != null && fragmentPart.isNotEmpty()) {
                // Extract specific part of the schema using JSON Pointer
                extractSchemaFragment(externalSchema, fragmentPart)
            } else {
                // Use the entire schema
                externalSchema
            }
            
            // Process the external schema recursively to handle nested references
            val processedExternalSchema = processSchema(schemaToAdd, externalFile.parentFile ?: baseDir)
            
            val definitionName = extractDefinitionName(ref)
            definitions[definitionName] = processedExternalSchema
        }
        
        private fun extractSchemaFragment(schema: JsonNode, fragment: String): JsonNode {
            // Simple JSON Pointer implementation for extracting schema fragments
            // Handles paths like "/properties/name" or "/definitions/user"
            var current = schema
            val parts = fragment.split("/").filter { it.isNotEmpty() }

            for (part in parts) {
                current = current.get(part) ?: throw IllegalArgumentException(
                    "Invalid JSON Pointer fragment: $fragment"
                )
            }

            return current
        }

        /**
         * Reads a schema file, automatically detecting whether it's JSON or YAML based on file extension.
         */
        private fun readSchemaFile(file: File): JsonNode {
            return when (file.extension.lowercase()) {
                "yaml", "yml" -> yamlMapper.readTree(file)
                "json" -> jsonMapper.readTree(file)
                else -> {
                    // Try to parse as JSON first, then YAML if that fails
                    try {
                        jsonMapper.readTree(file)
                    } catch (e: Exception) {
                        try {
                            yamlMapper.readTree(file)
                        } catch (yamlException: Exception) {
                            throw IllegalArgumentException(
                                "Unable to parse file '${file.name}' as JSON or YAML. " +
                                "JSON error: ${e.message}, YAML error: ${yamlException.message}"
                            )
                        }
                    }
                }
            }
        }

        /**
         * Writes a schema to file, determining the format based on the output file extension.
         * YAML/YML extensions will output YAML format, otherwise JSON format.
         */
        private fun writeSchemaFile(file: File, schema: JsonNode) {
            when (file.extension.lowercase()) {
                "yaml", "yml" -> yamlMapper.writerWithDefaultPrettyPrinter().writeValue(file, schema)
                else -> jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, schema)
            }
        }
    }
}
