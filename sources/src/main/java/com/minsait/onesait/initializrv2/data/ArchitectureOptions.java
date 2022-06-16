package com.minsait.onesait.initializrv2.data;

import java.util.List;

import lombok.Data;

@Data
public class ArchitectureOptions {

	private String name;
	private boolean defaultValue;
	private String description;
	private String url;
	private List<ArchitectureOptionsFiles> files;

	@Data
	public static class ArchitectureOptionsFiles {
		private String mustache;
		private String dest;
		private FileType type;
		private String name;
		private List<String> scopes;
	}
	
	public enum FileType {
		CLASS, RESOURCE, FILE,TEST
	}
}
