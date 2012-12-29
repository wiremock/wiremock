/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.http;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;

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
