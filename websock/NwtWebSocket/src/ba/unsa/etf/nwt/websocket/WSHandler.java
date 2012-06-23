package ba.unsa.etf.nwt.websocket;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;

public class WSHandler extends WebSocketHandler {

	private final Set<KlijentSock> klijenti = new CopyOnWriteArraySet<KlijentSock>();

	public WebSocket doWebSocketConnect(HttpServletRequest request,	String protocol) {
		return new KlijentSock();
	}

	private class KlijentSock implements WebSocket.OnTextMessage, WebSocket.OnBinaryMessage {

		private Connection connection;

		// Klijent (Browser) je otvorio konekciju
		public void onOpen(Connection connection) {
			this.connection = connection;
			this.connection.setMaxBinaryMessageSize(1024 * 512);
			// Dodaj ovu konekciju u listu konekcija
			klijenti.add(this);
		}

		// Kada primimo poruku
		public void onMessage(String data) {
			try {
				// Prodji kroz sve aktivne klijente i proslijedi im poruku
				for (KlijentSock w : klijenti) {
					// posalji poruku trenutnom klijentu
					w.connection.sendMessage(data);
				}
			} catch (IOException x) {
				this.connection.disconnect();
			}

		}

		public void onClose(int closeCode, String message) {
			// Ukloni klijent iz liste
			klijenti.remove(this);
		}
		
		// Binarna poruka
		public void onMessage(byte[] data, int offset, int len) {
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			bOut.write(data, offset, len);
			try {
			
				
				InputStream in = new ByteArrayInputStream(bOut.toByteArray());
				BufferedImage image = ImageIO.read(in);
			    LuminanceSource source = new BufferedImageLuminanceSource(image);
			    BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
			    
				Reader reader = new MultiFormatReader();
				Result result = null;
				try {
					result = reader.decode(bitmap);
					if (result!=null) 
						this.connection.sendMessage(result.getText());
				} catch (Exception e) {}
				
			} catch (IOException e) {
				Log.warn(e.getMessage());
			}
		}
	}
}
