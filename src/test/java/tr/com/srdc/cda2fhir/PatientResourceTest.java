package tr.com.srdc.cda2fhir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Patient;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ICDATransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

public class PatientResourceTest {
    @BeforeClass
    public static void init() {
    	// Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }
    
    private static Bundle generateBundle(String sourceName) throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
        ClinicalDocument cda = CDAUtil.load(fis);

        ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Config.setGenerateDafProfileMetadata(true);
        Config.setGenerateNarrative(true);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        return bundle;
    }
    
    private static void verifyPatient(Bundle bundle, String baseName) throws IOException, JSONException {
    	Assert.assertNotNull(bundle);
    	FHIRUtil.printJSON(bundle, "src/test/resources/output/" + baseName + ".json");
    	
    	List<Patient> patients = bundle.getEntry().stream()
    								.map(r -> r.getResource())
    								.filter(r -> (r instanceof Patient))
    								.map(r -> (Patient) r)
    								.collect(Collectors.toList());
    	Assert.assertEquals(1, patients.size());
    	Patient patient = patients.get(0);
    	String actual = FHIRUtil.encodeToJSON(patient);
    	FHIRUtil.printJSON(patient, "src/test/resources/output/" + baseName + ".patient.json");
    	
    	Path goldFilePath = Paths.get("src/test/resources/gold/" + baseName + ".patient.json");
    	String expected = new String(Files.readAllBytes(goldFilePath));
    	JSONAssert.assertEquals(expected, actual, true);
    }
    
    private static void runGoldTest(String sourceName) throws Exception {
     	Bundle bundle = generateBundle(sourceName);   	
    	String baseName = sourceName.substring(0, sourceName.length() - 4);    	
        verifyPatient(bundle, baseName);   	
    }
    
    // Cerner/Person-RAKIA_TEST_DOC00001 (1).XML
    @Test
    public void testGoldSample() throws Exception {
    	runGoldTest("Cerner/Person-RAKIA_TEST_DOC00001 (1).XML");
    }
    
    @Test
    public void testGoldSampleJolt() throws Exception {
    	String sourceName = "Cerner/Person-RAKIA_TEST_DOC00001 (1).XML";
    	File file = new File("src/test/resources/" + sourceName);
    	String content = FileUtils.readFileToString(file, Charset.defaultCharset());    	
    	JSONObject recordTargetJson = XML.toJSONObject(content)
    						.getJSONObject("ClinicalDocument")
    						.getJSONObject("recordTarget");
    	String outputFile = "src/test/resources/output/" + "Cerner/Person-RAKIA_TEST_DOC00001 (1) - jolt.json";
    	FileUtils.writeStringToFile(new File(outputFile), recordTargetJson.toString(4), Charset.defaultCharset());    	
    	
        List<Object> chainrSpecJSON = JsonUtils.filepathToList("src/test/resources/jolt/patient.json");
        Chainr chainr = Chainr.fromSpec( chainrSpecJSON );
        Object inputJSON = JsonUtils.filepathToObject(outputFile);
        Object transformedOutput = chainr.transform(inputJSON);
        System.out.println(JsonUtils.toJsonString(transformedOutput));    	
    }
}
