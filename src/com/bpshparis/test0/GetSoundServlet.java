package com.bpshparis.test0;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet implementation class GetImportedKeysServlet
 */
@WebServlet("/GetSound")
public class GetSoundServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSoundServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		Map<String, Object> result = new HashMap<String, Object>();

		result.put("from", this.getServletName());

		try{

			Path realPath = Paths.get(getServletContext().getRealPath("/"));
			Path soundsPath = Paths.get(realPath + "/sounds");


			System.out.println("ServletFileUpload.isMultipartContent=" + ServletFileUpload.isMultipartContent(request));

			if(ServletFileUpload.isMultipartContent(request)){

				List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
				for (FileItem item : items) {
					if (!item.isFormField()){
						// item is the file (and not a field)
						//writeSoundFile(new BufferedInputStream(item.getInputStream()), audio);
						Path wav = Paths.get(soundsPath + "/" + item.getName());
						String fileName;
						if(item.getName().contains(".")){
							fileName = item.getName().substring(0, item.getName().lastIndexOf("."));
						}
						else{
							fileName = item.getName();
						}
						Path mp3 = Paths.get(soundsPath + "/" + fileName + ".mp3");
						Files.copy(new BufferedInputStream(item.getInputStream()), wav, StandardCopyOption.REPLACE_EXISTING);

						result.put("status", "OK");
						result.put("msg", "Record saved in " + wav + ".");
						
						Path lame = Paths.get("/usr/bin/lame");
						if(Files.exists(lame)){
							
							Files.deleteIfExists(mp3);

							ProcessBuilder pb = new ProcessBuilder();
							pb.directory(soundsPath.toFile());
							String[] cmds = {lame.toString(), "-V2", wav.toString(), mp3.toString()};
							pb.command(cmds);
							
						    Process process = pb.start();
						    process.waitFor();
						    
						    if(Files.exists(mp3)){
								result.put("status", "OK");
								result.put("msg", "Record saved in " + mp3 + ".");
						    }
							
						}
					}

				}
			}


		}
		catch(Exception e){
			result.put("status", "KO");
			result.put("msg", e.getMessage());
			e.printStackTrace();
		}


		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		StringWriter sw = new StringWriter();
		String jsonResult = null;
		mapper.writeValue(sw, result);
		sw.flush();
		jsonResult = sw.toString();
		sw.close();
		response.getWriter().write(jsonResult);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
