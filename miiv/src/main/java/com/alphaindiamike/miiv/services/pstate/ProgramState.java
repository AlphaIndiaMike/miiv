package com.alphaindiamike.miiv.services.pstate;

public interface ProgramState {
	ProgramState handleStateChange(ProgramStateContext context, ProgramState nextState, String[] parameters);
    String getState();
}
