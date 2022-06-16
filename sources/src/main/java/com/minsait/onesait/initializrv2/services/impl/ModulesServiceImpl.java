package com.minsait.onesait.initializrv2.services.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.minsait.onesait.initializrv2.exceptions.InitializrException;
import com.minsait.onesait.initializrv2.exceptions.ModuleNotFoundException;
import com.minsait.onesait.initializrv2.services.FilesUtils;
import com.minsait.onesait.initializrv2.services.ModulesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModulesServiceImpl implements ModulesService {

	private static final String OUT_PATH = "/tmp/tmpMod/{{UUID}}";

	private Map<String, String> modulesGit = new TreeMap<>();
	private Map<String, String[]> modulesDescriptions = new TreeMap<>();

	@Autowired
	private FilesUtils fileUtils;

	@PostConstruct
	protected void setup() {
		parseXML();
		fileUtils.generateFolder(OUT_PATH.replace("{{UUID}}", ""));
	}

	public byte[] generate(List<String> modules) throws ModuleNotFoundException, InitializrException {

		// generate temp out path
		String tempPath = OUT_PATH.replace("{{UUID}}", UUID.randomUUID().toString());
		fileUtils.generateFolder(tempPath);
		// comprobar y descargar modulos
		for (String mod : modules) {
			if (modulesGit.containsKey(mod)) {
				downloadFile(modulesGit.get(mod), tempPath);
			} else {
				throw new ModuleNotFoundException(HttpStatus.BAD_REQUEST,
						"Module {{KEY}} not found".replace("{{KEY}}", mod));
			}
		}

		// zip temp foder
		byte[] zipBytes = fileUtils.zipFolder(tempPath);

		// remove tempFolder
		fileUtils.removeDir(tempPath);

		return zipBytes;
	}

	public void downloadFile(String gitId, String outFolder) {
		try {
			HttpHeaders headers = new HttpHeaders();
			RestTemplate restTemplate = new RestTemplate();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<byte[]> response = restTemplate.exchange("https://api.github.com/repos/onesaitplatform/"
					+ gitId + "/zipball/master", HttpMethod.GET, entity, byte[].class);
			unZipFile(response.getBody(), outFolder);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private void unZipFile(byte[] bytes, String directorioSalida) throws FileNotFoundException, IOException {
		final int TAM_BUFFER = 4096;
		byte[] buffer = new byte[TAM_BUFFER];

		try(ZipInputStream flujo=new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entrada;
			String commitHash = "";
			while ((entrada = flujo.getNextEntry()) != null) {
				String nombreSalida = directorioSalida + File.separator + entrada.getName();
				if (commitHash.equals("")) {
					String [] spplitedName = nombreSalida.split("-");
					commitHash = spplitedName[spplitedName.length - 1];
				}
				nombreSalida = nombreSalida.replaceAll("-" + commitHash, "/");
				if (entrada.isDirectory()) {
					File directorio = new File(nombreSalida);
					directorio.mkdir();
				} else {					
					try(BufferedOutputStream salida= new BufferedOutputStream(new FileOutputStream(nombreSalida), TAM_BUFFER)) {
						int leido;
						while ((leido = flujo.read(buffer, 0, TAM_BUFFER)) != -1) {
							salida.write(buffer, 0, leido);
						}
					}
				}
			}
//			while ((entrada = flujo.getNextEntry()) != null) {
//				String nombreSalida = directorioSalida + File.separator + entrada.getName();
//				if (commitHash.equals("")) {
//					commitHash = nombreSalida.split("-initializr-")[1];
//				}
//				nombreSalida = nombreSalida.replaceAll("-initializr-" + commitHash, "/");
//				if (entrada.isDirectory()) {
//					File directorio = new File(nombreSalida);
//					directorio.mkdir();
//				} else {					
//					try(BufferedOutputStream salida= new BufferedOutputStream(new FileOutputStream(nombreSalida), TAM_BUFFER)) {
//						int leido;
//						while ((leido = flujo.read(buffer, 0, TAM_BUFFER)) != -1) {
//							salida.write(buffer, 0, leido);
//						}
//					}
//				}
//			}
		}
	}

	@Override
	public Map<String, String[]> getModulesDescriptions() {
		return modulesDescriptions;
	}

	private void parseXML() {
		try {
			InputStream in = getClass().getResourceAsStream("/InitializrConfiguration/modules-descriptions.xml");

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("module");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					modulesGit.put(eElement.getElementsByTagName("artifactId").item(0).getTextContent(),
							eElement.getElementsByTagName("gitId").item(0).getTextContent());
					String[] info = new String[2];
					info[0] = eElement.getElementsByTagName("descr").item(0).getTextContent();
					info[1] = eElement.getElementsByTagName("url").item(0).getTextContent();
					modulesDescriptions.put(eElement.getElementsByTagName("artifactId").item(0).getTextContent(), info);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
