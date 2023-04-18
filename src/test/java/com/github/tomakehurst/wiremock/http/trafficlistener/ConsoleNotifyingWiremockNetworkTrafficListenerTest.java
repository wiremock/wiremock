package com.github.tomakehurst.wiremock.http.trafficlistener;

import com.github.tomakehurst.wiremock.common.Notifier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConsoleNotifyingWiremockNetworkTrafficListenerTest {
    private final Notifier mockNotifier = mock(Notifier.class);
    
    @Test
    public void defaultConstructor_notifiesToSystemOutAndUsesUTF8Charset() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener();
        Socket socket = new Socket();
        ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_8);

        consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

        assertThat(out.toString(), containsString("Hello world"));
        
        System.setOut(originalOut);
    }

    @Test
    public void charsetConstructor_notifiesToSystemOutAndUsesSpecifiedCharset() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener(StandardCharsets.UTF_16);
        Socket socket = new Socket();
        ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_16);

        consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

        assertThat(out.toString(), containsString("Hello world"));

        System.setOut(originalOut);
    }
    
    @Test
    public void opened_withSocket_shouldNotifyAtInfo() {
        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
        Socket socket = new Socket();
        
        consoleNotifyingWiremockNetworkTrafficListener.opened(socket);
        
        verify(mockNotifier).info(contains("Opened "));
    }

    @Test
    public void closed_withSocket_shouldNotifyAtInfo() {
        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
        Socket socket = new Socket();

        consoleNotifyingWiremockNetworkTrafficListener.closed(socket);

        verify(mockNotifier).info(contains("Closed "));
    }

    @Test
    public void incoming_withBytebufferWithIncompatibleCharset_shouldNotifyBytesOmittedAtInfo() {
        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
        Socket socket = new Socket();
        ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_16);

        consoleNotifyingWiremockNetworkTrafficListener.incoming(socket, byteBuffer);

        verify(mockNotifier).error(contains("Incoming bytes omitted."));
    }

    @Test
    public void incoming_withBytebufferWithCompatibleCharset_shouldNotifyWithIncomingBytes() {
        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
        Socket socket = new Socket();
        ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_8);

        consoleNotifyingWiremockNetworkTrafficListener.incoming(socket, byteBuffer);

        verify(mockNotifier).info(contains("Hello world"));
    }

    @Test
    public void outgoing_withBytebufferWithIncompatibleCharset_shouldNotifyBytesOmittedAtInfo() {
        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
        Socket socket = new Socket();
        ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_16);

        consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

        verify(mockNotifier).error(contains("Outgoing bytes omitted."));
    }

    @Test
    public void outgoing_withBytebufferWithCompatibleCharset_shouldNotifyWithIncomingBytes() {
        ConsoleNotifyingWiremockNetworkTrafficListener consoleNotifyingWiremockNetworkTrafficListener
                = new ConsoleNotifyingWiremockNetworkTrafficListener(mockNotifier, StandardCharsets.UTF_8);
        Socket socket = new Socket();
        ByteBuffer byteBuffer = stringToByteBuffer("Hello world", StandardCharsets.UTF_8);

        consoleNotifyingWiremockNetworkTrafficListener.outgoing(socket, byteBuffer);

        verify(mockNotifier).info(contains("Hello world"));
    }
    
    public static ByteBuffer stringToByteBuffer(String msg, Charset charset){
        return ByteBuffer.wrap(msg.getBytes(charset));
    }
}
