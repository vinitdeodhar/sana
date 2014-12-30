package org.moca.net;

/**
 * Characterizes a result from the MDS. The response is a JSON dictionary with two keys.
 * 	
 * 	- `status' : either SUCCESS or FAILURE, depending on whether the request succeeded
 *  - `code' : the code indicating what the result was
 *  - `data' : miscellaneous data pertaining to the request 
 */
public class MDSResult {
	private static final String SUCCESS_STRING = "SUCCESS";
	private static final String FAILURE_STRING = "FAILURE";
	
	private String status;
	private String code;
	private String data;
	
	MDSResult() {
		status = "none";
		code = "";
		data = "";
	}
	
	public boolean succeeded() {
		return SUCCESS_STRING.equals(status);
	}
	
	public boolean failed() {
		return FAILURE_STRING.equals(status);
	}
	
	public String getData() {
		return data;
	}
	
	public String getCode() {
		return code;
	}
}
