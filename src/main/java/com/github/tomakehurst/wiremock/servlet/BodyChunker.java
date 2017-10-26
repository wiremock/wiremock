package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;

import java.util.Arrays;

public class BodyChunker {

    private static final Notifier notifier = new Slf4jNotifier(false);

    public static byte[][] chunkBody(byte[] body, int numberOfChunks) {

        if (numberOfChunks < 1) {
            notifier.error("Number of chunks set to value less than 1: " + numberOfChunks);
            numberOfChunks = 1;
        }

        if (body.length < numberOfChunks) {
            notifier.error("Number of chunks set to value greater then body length. Number of chunks: " + numberOfChunks +
                    ". Body length: " + body.length + ". Overriding number of chunks to body length.");
            numberOfChunks = body.length;
        }

        int chunkSize = body.length / numberOfChunks;
        int excessSize = body.length % numberOfChunks;

        byte[][] chunkedBody = new byte[numberOfChunks][];

        for (int chunkIndex = 0; chunkIndex < numberOfChunks; chunkIndex++) {
            int chunkStart = chunkIndex * chunkSize;
            int chunkEnd = chunkStart + chunkSize;

            chunkedBody[chunkIndex] = Arrays.copyOfRange(body, chunkStart, chunkEnd);
        }

        if (excessSize > 0) {
            int lastChunkIndex = numberOfChunks - 1;

            int chunkStart = lastChunkIndex * chunkSize;
            int newChunkEnd = chunkStart + chunkSize + excessSize;

            chunkedBody[lastChunkIndex] = Arrays.copyOfRange(body, chunkStart, newChunkEnd);
        }

        return chunkedBody;
    }
}
