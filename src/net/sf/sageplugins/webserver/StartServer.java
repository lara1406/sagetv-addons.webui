/*
 * Created on Oct 28, 2004
 *
 * 
 *
 */
package net.sf.sageplugins.webserver;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.sageplugins.sageutils.SageApi;

import Acme.Serve.Serve;
/**
 * @author Owner
 *
 * 
 *
 */
public class StartServer implements Runnable {
	public StartServer() {
	}
	public void run(){
		try {
		Vector<String> args=new Vector<String>();
		String s = null;
		args.add("-p");
		args.add(SageApi.GetProperty("nielm/webserver/port","8080"));
		args.add("-root");
		args.add(SageApi.GetProperty("nielm/webserver/root","webserver"));
		
		s=SageApi.GetProperty("nielm/webserver/throttles_file","throttles.properties");
		if ( !s.equals("") ){
			args.add("-t");
			args.add(s);
		}
	
		args.add("-s");
		args.add(SageApi.GetProperty("nielm/webserver/servlets_file","servlets.properties"));
		
		
		s=SageApi.GetProperty("nielm/webserver/realms_file","realms.properties");
		if ( !s.equals("") ){
			args.add("-r");
			args.add(s);
		}
		
		s=SageApi.GetProperty("nielm/webserver/aliases_file","aliases.properties");
		if ( !s.equals("") ){
			args.add("-a");
			args.add(s);
		}
		
		s=SageApi.GetProperty("nielm/webserver/bind_address","");
		if ( ! s.equals("")){
			args.add("-b");
			args.add(s);
		}
		
		s=SageApi.GetProperty("nielm/webserver/backlog","");
		if ( ! s.equals("")){
			args.add("-k");
			args.add(s);
		}
		
		s=SageApi.GetProperty("nielm/webserver/cgibin_dir","");
		if ( ! s.equals("")){
			args.add("-c");
			args.add(s);
		}
		s=SageApi.GetProperty("nielm/webserver/session_timeout","");
		if ( ! s.equals("")){
			args.add("-e");
			args.add(s);
		}
		s="";
		if ( SageApi.GetBooleanProperty("nielm/webserver/log_access",false)) {
			s=s+"-l";
			if ( SageApi.GetBooleanProperty("nielm/webserver/log_user_agent",false))
				s=s+"a";
			if ( SageApi.GetBooleanProperty("nielm/webserver/log_referrer",false))
				s=s+"r";
			args.add(s);
		} else {
			// get the properties anyway to populate properties file
			SageApi.GetBooleanProperty("nielm/webserver/log_user_agent",false);
			SageApi.GetBooleanProperty("nielm/webserver/log_referrer",false);
		}
		
		
		if ( SageApi.GetBooleanProperty("nielm/webserver/use_ssl",false)) {
			args.add("-socketFactory");
			args.add("Acme.Serve.SSLServerSocketFactory");
			s=SageApi.GetProperty("nielm/webserver/ssl_args","");
			StringTokenizer st = new StringTokenizer(s, " ");
			while( st.hasMoreTokens())
				args.add(st.nextToken());
		} else {
			SageApi.GetProperty("nielm/webserver/ssl_args","");
		}
		
		args.add("-nohup");
		String[] strarr=new String[args.size()];
		strarr=(String[])args.toArray(strarr);
		System.out.println("Starting webserver with args: "+args.toString());
		Serve.main(strarr);
		} catch (Throwable e) {
			System.out.println("Failed to start webserver: "+e.toString());
			e.printStackTrace();
		}
	}
}
