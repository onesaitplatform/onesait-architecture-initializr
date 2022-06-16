package com.minsait.onesait.initializrv2.data;

import java.util.List;

import com.minsait.onesait.initializrv2.data.ArchitectureOptions.ArchitectureOptionsFiles;

import lombok.Data;

@Data
public class LibrariesConfiguration {

	private List<ArchitectureOptions> optionals;
	private List<ArchitectureOptionsFiles> defaults;
	private List<ArchitectureOptionsFiles> tests;
}
