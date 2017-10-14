package de.doubleslash.hideAPI;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(value = Suite.class)
@SuiteClasses(value = { 
		TBSEncoderUnitTests.class,
		MessageHandlerUnitTests.class,
		ImageHandlerUnitTests.class,
		CryptUnitUnitTests.class
})
public class UnitTestingSuite {}
