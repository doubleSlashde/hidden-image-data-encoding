package de.doubleslash.hideAPI;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(value = Suite.class)
@SuiteClasses(value = { 
		CustomMetadataIntegrationTest.class,
		SignatureIntegrationTest.class
})
public class EncodingIntegrationTestSuite {}
