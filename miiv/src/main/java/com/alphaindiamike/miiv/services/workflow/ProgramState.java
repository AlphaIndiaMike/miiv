package com.alphaindiamike.miiv.services.workflow;

public interface ProgramState {
	ProgramState handleStateChange(ProgramStateContext context, ProgramState nextState, String[] parameters);
    String getStateName();
}
