package ch.supertomcat.supertomcatutils.application;

import org.junit.jupiter.api.Test;

class ApplicationUtilTest {

	@Test
	void test() {
		ApplicationUtil.getThisApplicationsJarFilename(ApplicationMain.class);
	}

}
