/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saintmartinhospital.fhir.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *
 * @author julian.martinez
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {
	
	private static ApplicationContext context;

	public static ApplicationContext getApplicationContext() {
		return context;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ac) throws BeansException {
		context = ac;
	}
	
}
