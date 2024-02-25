package com.alphaindiamike.miiv.controllers.cli;

public record CommandResponse(
		String Response,
		String Error,
		boolean PR
		) {
}
