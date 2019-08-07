
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import java.util.Set;

public class OWLMethods {

	public static OWLDataFactory factory = SBMLreactionFinder.manager.getOWLDataFactory();

	public OWLMethods() {
	}

	
	public static Set<String> getClsValueObjectProperty(OWLOntology ont, String clsuri, String propuri) {
		Set<String> valset = new HashSet<String>();
		Set<OWLClassExpression> clsex = factory.getOWLClass(IRI.create(clsuri)).getSuperClasses(ont);
		for(OWLClassExpression oneclsex : clsex){
			if(oneclsex instanceof OWLObjectSomeValuesFrom){
				OWLObjectSomeValuesFrom hasvalex = (OWLObjectSomeValuesFrom)oneclsex;
				if(hasvalex.getProperty().asOWLObjectProperty().getIRI().toString().equals(propuri)){
					valset.add(hasvalex.getFiller().asOWLClass().getIRI().toString());
				}
			}
		}
		return valset;
	}
	
	
	public static Set<String> getClsValueDataProperties(OWLOntology ont, String clsuri, String propuri) {
		Set<String> valset = new HashSet<String>();
		OWLClass someclass = factory.getOWLClass(IRI.create(clsuri));
		for(OWLClassExpression oneclsex : someclass.getSuperClasses(ont)){
			if(oneclsex instanceof OWLDataHasValue){
				OWLDataHasValue hasvalex = (OWLDataHasValue)oneclsex;
				if(hasvalex.getProperty().asOWLDataProperty().getIRI().toString().equals(propuri)){
					valset.add(hasvalex.getValue().getLiteral());
				}
			}
		}
		return valset;
	}
	
	

	public static void addClass(OWLOntology ont, Hashtable urisandparenturis,
			OWLOntologyManager manager) {
		String[] keyset = new String[urisandparenturis.size()];

		keyset = (String[]) urisandparenturis.keySet().toArray(keyset);
		for (int i = 0; i < keyset.length; i++) {
			String[] parentnames = (String[]) urisandparenturis.get(keyset[i]);
			for (int x = 0; x < parentnames.length; x++) {
				OWLClass parent = factory.getOWLClass(IRI.create(parentnames[x]));
				OWLClass classtoadd = factory.getOWLClass(IRI.create(keyset[i]));
				OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classtoadd,parent);
				AddAxiom addAxiom = new AddAxiom(ont, axiom);
				manager.applyChange(addAxiom);
			}
		}
	}
	
	
	public static void addClass(OWLOntology ont, OWLClass classtoadd, OWLClass parent,
		OWLOntologyManager manager) {

			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classtoadd,parent);
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
	}
	

	// SET A CLASS' DATATYPE PROPERTY
	public static void setClsDatatypeProperty(OWLOntology ont, String clsname,
			String rel, String value, OWLOntologyManager manager)
			throws OWLException {
		if(!value.equals("")){
			OWLClass cls = factory.getOWLClass(IRI.create(clsname));
			OWLDataProperty theprop = factory.getOWLDataProperty(IRI.create(rel));
			OWLLiteral valueconstant = factory.getOWLTypedLiteral(value);
	
			OWLClassExpression propdesc = factory.getOWLDataHasValue(theprop,
					valueconstant);
			OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, propdesc);
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}

	
	public static void setClsObjectProperty(OWLOntology ont,
			OWLClass cls, OWLClass value, String rel, String invrel,
			Boolean applyinverse, OWLOntologyManager manager) {
		
			OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(rel));
			OWLClassExpression prop_val = factory.getOWLObjectSomeValuesFrom(prop, value);
			OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, prop_val);
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);

			if (applyinverse) {
				OWLObjectProperty invprop = factory.getOWLObjectProperty(IRI.create(invrel));
				OWLClassExpression invprop_cls = factory.getOWLObjectSomeValuesFrom(invprop, cls);
				OWLSubClassOfAxiom invaxiom = factory.getOWLSubClassOfAxiom(value, invprop_cls);
				AddAxiom invAddAxiom = new AddAxiom(ont, invaxiom);
				manager.applyChange(invAddAxiom);
			}
	}
	
	



	public static String[] getRDFLabels(OWLOntology ont, OWLEntity ent) {
		OWLLiteral val = null;
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		OWLAnnotation[] annarray = ent.getAnnotations(ont, label).toArray(new OWLAnnotation[] {});
		if (annarray.length == 0) {
			return new String[] { "" };
		} else {
			String[] labeltexts = new String[annarray.length];

			for (int x = 0; x < annarray.length; x++) {
				val = (OWLLiteral) annarray[x].getValue();
				labeltexts[x] = val.getLiteral();
			}
			return labeltexts;
		}
	}
	
	
	public static String[] getRDFLabelAndSynonyms(OWLOntology ont, OWLEntity cls){
		ArrayList<String> labelandsyns = new ArrayList<String>();
		String label = getRDFLabels(ont, cls)[0];
		if(!label.equals("")){
			labelandsyns.add(label);
			for(String syn : getClsValueDataProperties(ont, cls.getIRI().toString(), SBMLreactionFinder.base + "synonym")){
				labelandsyns.add(syn);
			}
		}
		return labelandsyns.toArray(new String[]{});
	}

	public static void setRDFLabel(OWLOntology ontology, Hashtable urisandvals,
			OWLOntologyManager manager) {
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		String[] keys = (String[]) urisandvals.keySet().toArray(new String[] {});
		for (int i = 0; i < keys.length; i++) {
			Object[] val = (Object[]) urisandvals.get(keys[i]);
			for (int y = 0; y < val.length; y++) {

				OWLNamedIndividual annind = factory.getOWLNamedIndividual(IRI.create(keys[i]));
				Set<OWLAnnotation> anns = annind.getAnnotations(ontology, label);
				for (OWLAnnotation ann : anns) {
					OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(
							(OWLAnnotationSubject) annind, ann);
					manager.applyChange(new RemoveAxiom(ontology, removeax));
				}
				String[] array = (String[]) urisandvals.get(keys[i]);
				OWLAnnotation newann = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), factory.getOWLStringLiteral(array[0], "en"));
				OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(annind.getIRI(), newann);
				manager.applyChange(new AddAxiom(ontology, ax));
			}
		}
	}

	public static void setRDFLabel(OWLOntology ontology,
			OWLNamedIndividual annind, String value, OWLOntologyManager manager) {
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		Set<OWLAnnotation> anns = annind.getAnnotations(ontology, label);
		for (OWLAnnotation ann : anns) {
			OWLAnnotationSubject annsub = annind.getIRI();
			OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(annsub, ann);
			manager.applyChange(new RemoveAxiom(ontology, removeax));
		}
		OWLAnnotation newann = factory.getOWLAnnotation(
				factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), factory.getOWLStringLiteral(value, "en"));
		OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(annind.getIRI(), newann);
		manager.applyChange(new AddAxiom(ontology, ax));
	}

	public static void setRDFLabel(OWLOntology ontology, OWLClass ent,
			String value, OWLOntologyManager manager) {
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		Set<OWLAnnotation> anns = ent.getAnnotations(ontology, label);
		for (OWLAnnotation ann : anns) {
			OWLAnnotationSubject annsub = ent.getIRI();
			OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(annsub, ann);
			manager.applyChange(new RemoveAxiom(ontology, removeax));
		}
		OWLAnnotation newann = factory.getOWLAnnotation(
				factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), factory.getOWLStringLiteral(value, "en"));
		OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(), newann);
		manager.applyChange(new AddAxiom(ontology, ax));
	}


	public static String getNamespaceFromIRI(String uri) {
		if (uri.contains("#")) {
			return uri.substring(0, uri.indexOf("#") + 1);
		} else {
			String fragment = uri.substring(uri.lastIndexOf("/"), uri.length());
			if (fragment.contains("_")) {
				return uri.substring(0, uri.lastIndexOf("_"));
			} else {
				return uri.substring(0, uri.lastIndexOf("/") + 1);
			}
		}
	}

	

	// REPLACING SPECIAL CHARACTERS THAT SHOULDN'T BE USED IN A URI
	public static String URIencoding(String word) {
		String result = word;
		word = word.replace(" ", "_");
		try {
			result = URLEncoder.encode(word, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}