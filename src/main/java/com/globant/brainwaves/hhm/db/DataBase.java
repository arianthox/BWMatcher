package com.globant.brainwaves.hhm.db;

import java.util.List;

/**
 * @author
 */
public interface DataBase {

	public void setType(String type);

	public List<String> readRegistered();

	public Model readModel(String name) throws Exception;

	public void saveModel(Model m, String name) throws Exception;
}
