package com.github.tomakehurst.wiremock.http.trafficlistener;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConsoleNotifyingWiremockNetworkTrafficListenerTest {

    @Test
    void decodeBytes() throws CharacterCodingException {
        String inputStr = "hello world";
        byte[] helloWorldBytes = inputStr.getBytes(StandardCharsets.UTF_8);
        ByteBuffer input = ByteBuffer.wrap(helloWorldBytes);
        String actual = ConsoleNotifyingWiremockNetworkTrafficListener.decodeBytes(input);
        assertThat(actual, is(inputStr));
    }

    @Test
    void decodeBytesForUtf8() throws CharacterCodingException {
        String inputStr = "hello ക world";
        ByteBuffer input = ByteBuffer.wrap(inputStr.getBytes(StandardCharsets.UTF_8));
        String actual = ConsoleNotifyingWiremockNetworkTrafficListener.decodeBytes(input);
        assertThat(actual, is(inputStr));
    }

    @Test
    void decodeBytesForUtf16() throws CharacterCodingException {
        String inputStr = "hello ക world";
        byte[] inputBytes = inputStr.getBytes(StandardCharsets.UTF_16);
        ByteBuffer input = ByteBuffer.wrap(inputBytes);
        String inputAsUtf8 = new String(inputBytes, StandardCharsets.UTF_8);
        String actual = ConsoleNotifyingWiremockNetworkTrafficListener.decodeBytes(input);
        assertThat(actual, is("(erroneous bytes dropped) " + inputAsUtf8));
    }

    @Test
    void decodeBytesIncorrect() throws CharacterCodingException {
        byte[] incorrect = {(byte) 'H', -75, -75, -75, (byte) 'I'};
        ByteBuffer input = ByteBuffer.wrap(incorrect);
        String actual = ConsoleNotifyingWiremockNetworkTrafficListener.decodeBytes(input);
        assertThat(actual, is("(erroneous bytes dropped) ���I"));
    }
}