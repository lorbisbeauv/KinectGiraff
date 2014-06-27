package thirdParty;

import semiautonomy.SingletonCommonDataImpl;

public class DvpisApi {

	/**
	 * @param args
	 */
	SingletonCommonDataImpl m_commonData;
	public DvpisApi(String command){
		m_commonData= SingletonCommonDataImpl.getSingletonObject();
	}
	public void setCommand(String cmd){
		m_commonData.testApiCommand=cmd;
	}


}
