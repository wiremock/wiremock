package com.tomakehurst.wiremock.http;

import java.io.IOException;
import java.net.Socket;

import javax.servlet.http.HttpServletResponse;

public enum Fault {

	EMPTY_RESPONSE {
		@Override
		public void apply(HttpServletResponse response, Socket socket) {
			try {
				socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	},
	
	MALFORMED_RESPONSE_CHUNK {
		@Override
		public void apply(HttpServletResponse response, Socket socket) {
			try {
				response.setStatus(200);
				response.flushBuffer();
				socket.getOutputStream().write("lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes());
				socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
	},
	
	RANDOM_DATA_THEN_CLOSE {
		@Override
		public void apply(HttpServletResponse response, Socket socket) {
			try {
				socket.getOutputStream().write("lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes());
				socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
	};
	
	public abstract void apply(HttpServletResponse response, Socket socket);
}
