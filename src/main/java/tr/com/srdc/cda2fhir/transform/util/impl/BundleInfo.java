package tr.com.srdc.cda2fhir.transform.util.impl;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.IIdentifierMap;

public class BundleInfo implements IBundleInfo {
	private IResourceTransformer resourceTransformer;
	private Map<String, String> idedAnnotations = new HashMap<String, String>();
	private IIdentifierMap<Reference> identifiedReferences = new IdentifierMap<Reference>();

	private CDAIIMap<IEntityResult> entities = new CDAIIMap<IEntityResult>();
	
	public BundleInfo(IResourceTransformer resourceTransformer) {
		this.resourceTransformer = resourceTransformer;
	}

	@Override
	public IResourceTransformer getResourceTransformer() {
		return resourceTransformer;
	}
	
	@Override
	public Map<String, String> getIdedAnnotations() {
		return idedAnnotations;		
	}
	
	public void mergeIdedAnnotations(Map<String, String> newAnnotations) {
		idedAnnotations.putAll(newAnnotations);
	}
	
	@Override
	public Reference getReferenceByIdentifier(String fhirType, Identifier identifier) {
		return identifiedReferences.get(fhirType, identifier);
	}
	
	public void putReference(String fhirType, Identifier identifier, Reference reference) {
		identifiedReferences.put(fhirType, identifier, reference);
	}

	@Override
	public IEntityResult findEntityResult(II ii) {
		return entities.get(ii);
	}
}
