package com.github.tomakehurst.wiremock.servlet;

import org.junit.Test;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BodyChunkerTest {

    @Test
    public void returnsBodyAsSingleChunkWhenChunkSizeIsOne() {
        byte[] body = "1234".getBytes();
        int numberOfChunks = 1;

        byte[][] chunkedBody = BodyChunker.chunkBody(body, numberOfChunks);

        assertThat(chunkedBody, arrayWithSize(numberOfChunks));
        assertThat(chunkedBody[0], equalTo(body));
    }

    @Test
    public void returnsEvenlyChunkedBody() {
        byte[] body = "1234".getBytes();
        int numberOfChunks = 2;

        byte[][] chunkedBody = BodyChunker.chunkBody(body, numberOfChunks);

        assertThat(chunkedBody, arrayWithSize(numberOfChunks));
        assertThat(chunkedBody[0], equalTo("12".getBytes()));
        assertThat(chunkedBody[1], equalTo("34".getBytes()));
    }

    @Test
    public void addsExcessBytesToLastChunk() {
        byte[] body = "1234".getBytes();
        int numberOfChunks = 3;

        byte[][] chunkedBody = BodyChunker.chunkBody(body, numberOfChunks);

        assertThat(chunkedBody, arrayWithSize(numberOfChunks));
        assertThat(chunkedBody[0], equalTo("1".getBytes()));
        assertThat(chunkedBody[1], equalTo("2".getBytes()));
        assertThat(chunkedBody[2], equalTo("34".getBytes()));
    }

    @Test
    public void defaultsChunkSizeToOneIfNumberOfChunksGreaterThenBodyLength() {
        byte[] body = "1234".getBytes();
        int numberOfChunks = 5;

        byte[][] chunkedBody = BodyChunker.chunkBody(body, numberOfChunks);

        assertThat(chunkedBody, arrayWithSize(body.length));
        assertThat(chunkedBody[0], equalTo("1".getBytes()));
        assertThat(chunkedBody[1], equalTo("2".getBytes()));
        assertThat(chunkedBody[2], equalTo("3".getBytes()));
        assertThat(chunkedBody[3], equalTo("4".getBytes()));
    }

    @Test
    public void defaultsChunkSizeToOneIfNumberOfChunksLessThanOne() {
        byte[] body = "1234".getBytes();
        int numberOfChunks = -1;

        byte[][] chunkedBody = BodyChunker.chunkBody(body, numberOfChunks);

        assertThat(chunkedBody, arrayWithSize(1));
        assertThat(chunkedBody[0], equalTo(body));
    }
}