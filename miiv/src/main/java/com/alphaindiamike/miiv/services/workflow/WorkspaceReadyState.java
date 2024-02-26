package com.alphaindiamike.miiv.services.workflow;

public class WorkspaceReadyState implements ProgramState{
	private String activeWorkspacePath = "";
	
	public WorkspaceReadyState(String workspace_path) {
		activeWorkspacePath=workspace_path;
	}
	
	@Override
	public String getStateName() {
		return "Ready";
	}

	@Override
	public ProgramState handleStateChange(ProgramStateContext context, ProgramState nextState, String[] parameters) {
		
		return null;
	}

}
