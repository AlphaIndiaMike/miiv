package com.alphaindiamike.miiv.services.workflow;

public class ProgramBusyState implements ProgramState{
	
	private String srcDir = "";
	private String destDir = "";
	private enum opType {
			COPY,
			MOVE
	}
	
	public ProgramBusyState(String destDir, String sourceDir, String OpType) {
		
	}
	
	@Override
	public String getStateName() {
		return "Busy";
	}

	@Override
	public ProgramState handleStateChange(ProgramStateContext context, ProgramState nextState, String[] parameters) {
		
		
		return new WorkspaceReadyState(destDir);
	}

}
