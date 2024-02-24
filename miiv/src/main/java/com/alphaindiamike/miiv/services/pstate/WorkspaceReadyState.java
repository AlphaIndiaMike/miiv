package com.alphaindiamike.miiv.services.pstate;

public class WorkspaceReadyState implements ProgramState{
	private String activeWorkspacePath = "";
	
	public WorkspaceReadyState(String workspace_path) {
		activeWorkspacePath=workspace_path;
	}
	
	@Override
	public String getState() {
		return "Ready";
	}

	@Override
	public ProgramState handleStateChange(ProgramStateContext context, ProgramState nextState, String[] parameters) {
		
		return new ProgramBusyState(parameters[0], activeWorkspacePath, parameters[1]);
	}

}
