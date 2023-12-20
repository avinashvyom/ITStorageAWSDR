package com.vyomlabs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LambdaFunctionOutput {
	private int statusCode;
	private String body;
}
