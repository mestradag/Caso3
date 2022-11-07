package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class ClientThread extends Thread{

    private BufferedReader pIn;
    private PrintWriter pOut;
    private SecurityFunctions f;

    public ClientThread(BufferedReader pIn, PrintWriter pOut){
        this.pIn = pIn;
        this.pOut = pOut;

        f = new SecurityFunctions();
    }

    public void run(){
		try {
            //loads public key
            PublicKey publicaServidor = f.read_kplus("datos_asim_srv.pub");

            //first step. we send SECURE INIT to the server
            pOut.println("SECURE INIT");

            //third step. we receive g, p, g_x and signature
            String G  = pIn.readLine();
            String P = pIn.readLine();
            String Gx = pIn.readLine();
            String sign = pIn.readLine();

            //fourth step, it verifies the signature
            String msj = G+","+P+","+Gx;
            byte[] byteSign = str2byte(sign);

            boolean authenticated = f.checkSignature(publicaServidor, byteSign, msj);

            System.out.println("Was signature verified? "+authenticated);

            //if the signature was authenticated, then:
            if(authenticated){
                //fifth step. we send ok to the server
                pOut.println("OK");

                //sixth.a step. we generate g_y
                SecureRandom r = new SecureRandom();
                int y = Math.abs(r.nextInt());
                
                Long longy = Long.valueOf(y);
                BigInteger biy = BigInteger.valueOf(longy);

                BigInteger biG = new BigInteger(G);
                BigInteger biP = new BigInteger(P);
                BigInteger g2x = new BigInteger(Gx);

                BigInteger g2y = G2Y(biG,biy,biP);
                String strG2y = g2y.toString();

                //sixth.b step. it sends g_y to the server
                pOut.println(strG2y);

                //seventh step, we calculate master key
                BigInteger llave_maestra = calcular_llave_maestra(g2x,biy,biP);
                String str_llave = llave_maestra.toString();

                // generating symmetric key
                SecretKey sk_srv = f.csk1(str_llave);
                SecretKey sk_mac = f.csk2(str_llave);

                //user inputs number to be sent
                //BufferedReader br = new BufferedReader (new InputStreamReader(System.in));
                //System.out.print("Message: ");
		        String msgInt = "4";

                String str_valor = new String(msgInt);
                byte[] byte_valor = str_valor.getBytes();
                
                //creation of iv1
                byte[] iv1 = generateIvBytes();
                String str_iv1 = byte2str(iv1);
                IvParameterSpec ivSpec1 = new IvParameterSpec(iv1);

                //eight step, we send encrypted message, hmac and iv1
                byte[] rta_consulta = f.senc(byte_valor, sk_srv,ivSpec1);
                byte [] rta_mac = f.hmac(byte_valor, sk_mac);
                String m1 = byte2str(rta_consulta);
                String m2 = byte2str(rta_mac);

                pOut.println(m1);
                pOut.println(m2);
                pOut.println(str_iv1);

                //tenth step, it checks if it's OK or ERROR
                String status = pIn.readLine();
                if(status.equals("OK")){
                    //eleventh step, we decrypt response from server, hmac and iv2
                    String str_consulta = pIn.readLine();
                    String str_mac = pIn.readLine();
                    String str_iv2 = pIn.readLine();
                    byte[] byte_consulta = str2byte(str_consulta);
                    byte[] byte_mac = str2byte(str_mac);
                    
                    byte[] iv2 = str2byte(str_iv2);
                    IvParameterSpec ivSpec2 = new IvParameterSpec(iv2);
                    byte[] descifrado = f.sdec(byte_consulta, sk_srv,ivSpec2);

                    //twelfth step, it verifies the integrity of the info received
                    boolean verificar = f.checkInt(descifrado, sk_mac, byte_mac);
                    System.out.println("Integrity check: " + verificar);    		
                    if (verificar) {
                        //thirteenth step, we send an OK to the server
                        pOut.println("OK");

                        String str_original = new String(descifrado, StandardCharsets.UTF_8);
                        System.out.println(str_original);
                    }
                    else{
                        pOut.println("ERROR");
                    }
                }
            }
            else{
                pOut.println("ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] str2byte( String ss)
	{	
		// Encapsulamiento con hexadecimales
		byte[] ret = new byte[ss.length()/2];
		for (int i = 0 ; i < ret.length ; i++) {
			ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
		}
		return ret;
	}

    private BigInteger G2Y(BigInteger base, BigInteger exponente, BigInteger modulo) {
		return base.modPow(exponente,modulo);
	}

    private BigInteger calcular_llave_maestra(BigInteger base, BigInteger exponente, BigInteger modulo) {
		return base.modPow(exponente, modulo);
	}
	
    private byte[] generateIvBytes() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return iv;
	}

    public String byte2str( byte[] b )
	{	
		// Encapsulamiento con hexadecimales
		String ret = "";
		for (int i = 0 ; i < b.length ; i++) {
			String g = Integer.toHexString(((char)b[i])&0x00ff);
			ret += (g.length()==1?"0":"") + g;
		}
		return ret;
	}
}
