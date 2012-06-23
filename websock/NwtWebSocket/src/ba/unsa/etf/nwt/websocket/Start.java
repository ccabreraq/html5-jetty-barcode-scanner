package ba.unsa.etf.nwt.websocket;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class Start {

	public static void main(String[] args) {
		try {
			// Pokrecemo jetty server na portu 8088.
			Server server = new Server(8088);
			
			// pokrecemo WSHandler kao jetty instancu
			WSHandler chatWebSocketHandler = new WSHandler();
			chatWebSocketHandler.setHandler(new DefaultHandler());
			server.setHandler(chatWebSocketHandler);

			server.start();
			server.join();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
