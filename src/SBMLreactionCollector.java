import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;


public class SBMLreactionCollector {
	
	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLOntology ontology;
	public static OWLDataFactory factory = manager.getOWLDataFactory();
	public static String base = "http://www.bhi.washington.edu/BioModelsReactions#";
	public static IRI ontologyIRI = IRI.create("http://www.bhi.washington.edu/BioModelsReactions.owl");
	public static File logfile = new File("./log.txt");
	public static PrintWriter logfilewriter;
	public static String GObase = "http://purl.org/obo/owl/GO#";
	public static Set<String> GOtermsadded = new HashSet<String>();
	public static Set<String> modelids = new HashSet<String>();
	public static Set<String> modelnames = new HashSet<String>();
	public static Set<String> modelmetaids = new HashSet<String>();
	public static Namespace rdfns = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public static Namespace uniprotns = Namespace.getNamespace("http://purl.uniprot.org/core/");
//	static final ObjectMapper mapper = new ObjectMapper();
	static final String API_KEY = "24e0ee9a-54e0-11e0-9d7b-005056aa3316";
	public static Map<String,ArrayList<String>> GOidLabelMap = new HashMap<String,ArrayList<String>>();

	public static void collectReactions() throws JDOMException, IOException, OWLException, XMLStreamException{

		ontology = manager.createOntology(ontologyIRI);
		logfilewriter = new PrintWriter(new FileWriter(logfile));
		int numannotatedrxns = 0;
		int numunannotatedrxns = 0;
		
		OWLClass GOparentclass = factory.getOWLClass(IRI.create(base + "GeneOntologyReferenceConcept"));
		OWLClass unannotatedrxn = factory.getOWLClass(IRI.create(base + "UnannotatedReaction"));
		OWLClass modelclass = factory.getOWLClass(IRI.create(base + "curatedSBMLmodel"));
		OWLClass taxonparent = factory.getOWLClass(IRI.create(base + "Taxon"));
		
		File modeldir = new File("resources/curated_models/");
		
		// Read in locally-stored GO class info
		Scanner GOscan = new Scanner(new File("./resources/GO.csv"));
		System.out.println(GOscan.hasNext());

		GOscan.nextLine(); // bypass header
		
		
		while(GOscan.hasNext()) {
			String id;
			ArrayList<String> prefandsyn = new ArrayList<String>();
			String line = GOscan.nextLine();
			StringTokenizer ctoken = new StringTokenizer(line, ",");
			
			if(ctoken.hasMoreTokens()) { // If ID present
				id = ctoken.nextToken();
				id = id.substring(id.lastIndexOf("/")+1, id.length());
				if(ctoken.hasMoreTokens()) { // If preferred label present
					prefandsyn.add(ctoken.nextToken());
					
					if(ctoken.hasMoreTokens()) { // If synonyms present
						String syns = ctoken.nextToken();
						
						StringTokenizer bartoken = new StringTokenizer(syns,"|");
						
						while(bartoken.hasMoreTokens()) {
							prefandsyn.add(bartoken.nextToken());
						}
					}
					GOidLabelMap.put(id, prefandsyn);
				}
			}
		}
		GOscan.close();
		
		
		File[] SBMLmodels = modeldir.listFiles();
		SBMLreactionFinder.msgarea.setText("Building local knowledge base...0% complete");
		SBMLreactionFinder.msgbar.setValue(0);
		
		SBMLDocument sbmldoc = null;
		Model sbmlmodel = null;
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		
		Namespace bqbiolns = Namespace.getNamespace("bqbiol","http://biomodels.net/biology-qualifiers/");
		Namespace bqmodelns = Namespace.getNamespace("bqmodel","http://biomodels.net/model-qualifiers/");

		for(int i=0; i<SBMLmodels.length; i++){
			if(SBMLmodels[i].getName().startsWith("BIOMD")){ // && i < 4){
				System.out.println(SBMLmodels[i].getCanonicalPath());
				try {
					sbmldoc = new SBMLReader().readSBML(SBMLmodels[i].getAbsolutePath());
				} catch (Exception e) {e.printStackTrace();}
				
				sbmlmodel = sbmldoc.getModel();
				
				doc = builder.build(SBMLmodels[i]);
				
				Element root = doc.getRootElement();
				Namespace ns = doc.getRootElement().getNamespace();
				Element modelnode = root.getChild("model", ns);
				String modelname = "";
				
				if(modelnode.getAttributeValue("name")!=null && !modelnames.contains(modelnode.getAttributeValue("name"))){
					modelname = OWLMethods.URIencoding(modelnode.getAttributeValue("name").replace(" ", "_"));
					modelnames.add(modelnode.getAttributeValue("name"));
				}
				else if(modelnode.getAttributeValue("id")!=null && !modelids.contains(modelnode.getAttributeValue("id"))){
					modelname = OWLMethods.URIencoding(modelnode.getAttributeValue("id").replace(" ", "_"));
					modelids.add(modelnode.getAttributeValue("id"));
				}
				else if(modelnode.getAttributeValue("metaid")!=null && !modelmetaids.contains(modelnode.getAttributeValue("metaid"))){
					modelname = OWLMethods.URIencoding(modelnode.getAttributeValue("metaid").replace(" ", "_").replace("metaid_", ""));
					modelmetaids.add(modelnode.getAttributeValue("metaid"));
				}
				else{modelname = "_SBML_Model" + SBMLmodels[i].getName();}
				
				OWLClass onemodelclass = factory.getOWLClass(IRI.create(base + modelname));
				
				OWLMethods.setRDFLabel(ontology, onemodelclass, modelname, manager);
				OWLMethods.addClass(ontology, factory.getOWLClass(IRI.create(base + modelname)), modelclass, manager);
				OWLMethods.setClsDatatypeProperty(ontology, onemodelclass.getIRI().toString(), base + "hasBiomodelsID", SBMLmodels[i].getName().replace(".xml", ""), manager);
				
				if(sbmldoc.getModel()!=null){
					if(sbmlmodel.getNotesString()!=null){
						String strippednotes = sbmlmodel.getNotesString().replaceAll("\\<.*?>"," ");
						strippednotes = strippednotes.replaceAll("\n"," ");
						String biomodelsps = "This model originates from BioModels Database";
						if(strippednotes.contains(biomodelsps)){
							strippednotes = strippednotes.substring(0,strippednotes.indexOf("This model originates from BioModels Database"));
						}
						strippednotes = strippednotes.trim();
						while(strippednotes.contains("  ") || strippednotes.contains("\\t")){
							strippednotes = strippednotes.replaceAll("  ", " ");
							strippednotes = strippednotes.replaceAll("\\t", " ");
						}
						OWLMethods.setClsDatatypeProperty(ontology, onemodelclass.getIRI().toString(), base + "hasNotes", strippednotes, manager);
					}
				}

				
				// get the taxonomy annotation for this model - could be in "is" or "occursIn" tag
				String[] taxonandcommonnames = new String[]{""};
				if(modelnode.getChild("annotation", ns)!=null){
					Element annotation  = modelnode.getChild("annotation", ns);
					//if(!sbmlmodel.getNotes().equals("")){
					//	OWLMethods.setClsDatatypeProperty(ontology, onemodelclass.getIRI().toString(), base + "hasNotes", sbmlmodel.getNotesString(), manager);
					//}
					if(annotation.getChild("RDF",rdfns)!=null){
						Element rdfchild = annotation.getChild("RDF",rdfns);
						if(rdfchild.getChild("Description",rdfns)!=null){
							Element description = rdfchild.getChild("Description", rdfns);
							Iterator<Element> propit = null;
							ArrayList<Element> proplist = new ArrayList<Element>();
							// Looks like sometimes (as in 000000236) - there are multiple isVersionOf tags instead of multiple li tags
							if((description.getChildren("is",bqbiolns)!=null && description.getChildren("is",bqbiolns).size()!=0)
									|| (description.getChildren("is",bqmodelns)!=null && description.getChildren("is",bqmodelns).size()!=0)
									|| (description.getChildren("occursIn",bqbiolns)!=null && description.getChildren("occursIn", bqbiolns).size()!=0)){
								if((description.getChildren("is",bqbiolns)!=null && description.getChildren("is",bqbiolns).size()!=0)){
									proplist.addAll(description.getChildren("is",bqbiolns));
//									propit = description.getChildren("is",bqbiolns).iterator();
								}
								// Sometimes the "is" qualifier is used with the bqmodel namespace to set the taxonomic information 
								if((description.getChildren("is",bqmodelns)!=null && description.getChildren("is",bqmodelns).size()!=0)){
									proplist.addAll(description.getChildren("is",bqmodelns));
								}
								// "occursIn" overrides "is" annotation, if present
								if((description.getChildren("occursIn",bqbiolns)!=null && description.getChildren("occursIn", bqbiolns).size()!=0)){
									proplist.addAll(description.getChildren("occursIn",bqbiolns));
								}
								propit = proplist.iterator();
								while(propit.hasNext()){
									Element versionof = propit.next();
									if(versionof.getChild("Bag",rdfns)!=null){
										Element bag = versionof.getChild("Bag",rdfns);
										if(bag.getChildren("li", rdfns)!=null){
											List<Element> references = bag.getChildren("li", rdfns);
											Iterator<Element> refit = references.iterator();
											while(refit.hasNext()){
												String ann = refit.next().getAttributeValue("resource", rdfns);
												if(ann.contains("urn:miriam:taxonomy:")){
													taxonandcommonnames = queryUniProtTaxonomyAndProcess(ann.replace("urn:miriam:taxonomy:",""));
													if(!taxonandcommonnames[0].equals("")){
														System.out.println(taxonandcommonnames[0]);
														OWLClass taxonclass = factory.getOWLClass(IRI.create(base + OWLMethods.URIencoding(taxonandcommonnames[0])));
														if(!ontology.containsClassInSignature(taxonclass.getIRI())){
															OWLClass taxontoadd = factory.getOWLClass(IRI.create(base + OWLMethods.URIencoding(taxonandcommonnames[0])));
															OWLMethods.addClass(ontology, taxontoadd, taxonparent, manager);
															OWLMethods.setRDFLabel(ontology, taxontoadd, taxonandcommonnames[0], manager);
															for(int y=1; y<taxonandcommonnames.length; y++){
																OWLMethods.setClsDatatypeProperty(ontology, taxontoadd.getIRI().toString(), base + "hasOtherName", taxonandcommonnames[y], manager);
																System.out.println(taxonandcommonnames[y]);
															}
														}
														OWLMethods.setClsObjectProperty(ontology, onemodelclass, taxonclass, base + "occursInOrganism", "", false, manager);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				
				
				
				
				if(modelnode.getChild("listOfReactions", ns)!=null){
					Element rxnsnode = modelnode.getChild("listOfReactions", ns);
					List<Element> rxns  = rxnsnode.getChildren("reaction", ns);
					Iterator<Element> rxniterator = rxns.iterator();
					
					while(rxniterator.hasNext()){
						Boolean hasGOannotation = false;

						Element rxn = rxniterator.next();
						String rxnlabel = "";
						String rxnid = "";
						String rxnname = "";
						String rxnmetaid = "";
						
						if(rxn.getAttributeValue("id")!=null){
							rxnid =  rxn.getAttributeValue("id");
						}
						if(rxn.getAttributeValue("name")!=null){
							rxnname = rxn.getAttributeValue("name");
							
						}
						if(rxn.getAttributeValue("metaid")!=null){
							rxnmetaid = rxn.getAttributeValue("metaid");
						}
						if(rxnid.equals("") && rxnname.equals("") && rxnmetaid.equals("")){
							System.out.print("Couldn't create unique ID for " + rxn.toString());
						}
						else{
							if(!rxnid.equals("")){
								rxnlabel = OWLMethods.URIencoding(rxnid);
							}
							else if(!rxnname.equals("")){
								rxnlabel = OWLMethods.URIencoding(rxnname);
							}
							else if(!rxnmetaid.equals("")){
								rxnlabel = OWLMethods.URIencoding(rxnmetaid);
							}
						}
						
						OWLClass rxnclass = factory.getOWLClass(IRI.create(base + modelname + "_" + rxnlabel));
						
						// Get the GO annotation for the reaction
						OWLClass GOclass = null;
			                
						if(rxn.getChild("annotation", ns)!=null){
							Element annotation  = rxn.getChild("annotation", ns);
							if(annotation.getChild("RDF",rdfns)!=null){
								Element rdfchild = annotation.getChild("RDF",rdfns);
								if(rdfchild.getChild("Description",rdfns)!=null){
									Element description = rdfchild.getChild("Description", rdfns);
									
									// Looks like sometimes (as in 000000236) - there are multiple isVersionOf tags instead of multiple li tags
									if(description.getChildren("isVersionOf",bqbiolns)!=null){
										Iterator<Element> isversionit = description.getChildren("isVersionOf",bqbiolns).iterator();
										while(isversionit.hasNext()){
											Element versionof = isversionit.next();
											if(versionof.getChild("Bag",rdfns)!=null){
												Element bag = versionof.getChild("Bag",rdfns);
												if(bag.getChildren("li", rdfns)!=null){
													List<Element> references = bag.getChildren("li", rdfns);
													Iterator<Element> refit = references.iterator();
													while(refit.hasNext()){
														String ann = refit.next().getAttributeValue("resource", rdfns);
														// If the miriam annotation is a GO term or an ec-code (BRENDA)
														if(ann.contains("urn:miriam:obo.go:") || ann.contains("urn:miriam:ec-code:") 
																|| ann.contains("//identifiers.org/go/") || ann.contains("//identifiers.org/ec-code")) {
															
															ann = URLDecoder.decode(ann, "UTF-8");
															String annenc = "";
															// If it's a GO annotation
															if(ann.contains("urn:miriam:obo.go:") || ann.contains("//identifiers.org/go/")){
																hasGOannotation = true;
																
																if(ann.contains("GO:"))
																	ann = ann.substring(ann.indexOf("GO:"),ann.length());
															}
															// Use BRENDA web service to get the GO xref for ec-code annotations
// 															else if(ann.contains("urn:miriam:ec-code:") || ann.contains("//identifiers.org/ec-code")){
//																
//																ann = ann.substring(ann.lastIndexOf(":")+1,ann.length());
//																ArrayList<String> GOxrefs = BRENDAwebservice.getGOxrefsFromID(ann);
//																// If there is a GO xref 
//																if( ! GOxrefs.isEmpty()){
//																	if( ! GOxrefs.get(0).equals("0")){
//																		hasGOannotation = true;
//																		ann = GOxrefs.get(0);
//																	}
//																}
//															}
															// If we've found a GO annotation, add the GO class and the reaction to the KB, unless already there
															if(hasGOannotation){
																annenc = ann.replace(":", "_");
																GOclass = factory.getOWLClass(IRI.create(SBMLreactionFinder.GObase + annenc));
																OWLMethods.addClass(ontology, GOclass, GOparentclass, manager);
																OWLMethods.addClass(ontology, rxnclass, GOclass, manager);
																
																numannotatedrxns++;
																
																OWLMethods.setRDFLabel(ontology, rxnclass, rxnlabel, manager);
																OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "hasName", rxnname, manager);
																OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "hasId", rxnid, manager);
																OWLMethods.setClsObjectProperty(ontology, rxnclass, onemodelclass, base + "hasSourceModel", base + "hasReaction", false, manager);
	
																//if(!taxon.equals(""))OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "occursInOrganism", taxon, manager);
																//OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "hasKineticLawFormula", sbmlrxn.getKineticLaw().getFormula(), manager);
																// Get and store info for the GO class, if not already added, and not already available in the existing reaction ontology
																String[] labelandsyns = new String[]{};
																labelandsyns = OWLMethods.getRDFLabelAndSynonyms(SBMLreactionFinder.rxnont, GOclass);
																if(labelandsyns.length==0){
																	if( ! GOtermsadded.contains(annenc) && labelandsyns.length==0){
																		
																		// Lookup term in local GO table
																		if(GOidLabelMap.containsKey(annenc)) {
																			labelandsyns = GOidLabelMap.get(annenc).toArray(new String[] {});
																			System.out.println("Found term in GO table for " + labelandsyns[0]);
																		}
																		
																		//labelandsyns = queryBioPortalGOAndProcess(annenc);
																		if(labelandsyns.length>0){
																			setRDFLabelAndSynonyms(labelandsyns, GOclass, annenc);
																		}
																	}
																}
																else{
																	System.out.println("Using existing term from KB for " + labelandsyns[0]);
																	setRDFLabelAndSynonyms(labelandsyns, GOclass, annenc);
																}
															}
														}
													}
												}
											}	
										}
										if(!hasGOannotation){
											// If no annotations can be linked to a GO term, store as an unannotated reaction
											numunannotatedrxns++;
											OWLMethods.addClass(ontology, rxnclass, unannotatedrxn, manager);
											OWLMethods.setRDFLabel(ontology, rxnclass, rxnlabel, manager);
											OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "hasName", rxnname, manager);
											OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "hasId", rxnid, manager);
											OWLMethods.setClsObjectProperty(ontology, rxnclass, onemodelclass, base + "hasSourceModel", base + "hasReaction", false, manager);

											//OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "hasSourceModelURL", "http://www.ebi.ac.uk/biomodels/models-main/publ/" + SBMLmodels[i].getName(), manager);
											//if(!taxon.equals(""))OWLMethods.setClsDatatypeProperty(ontology, rxnclass.getIRI().toString(), base + "occursInOrganism", taxon, manager);

										}
									}
								}
							}
						}
						

//						Element kinlaw = rxn.getChild("kineticLaw", ns);
						
//						if(kinlaw.getChild("listOfParameters", ns)!=null && hasGOannotation){
//							Element listopars = kinlaw.getChild("listOfParameters", ns);
//							List<Element> pars = listopars.getChildren("parameter", ns);
//							Iterator<Element> parit = pars.iterator();
//							
//							while(parit.hasNext()){
//								Element nextpar = parit.next();
//								String parname = OWLMethods.URIencoding(nextpar.getAttributeValue("id").replace(" ", "_"));
//								String value = nextpar.getAttributeValue("value");
//								//System.out.println("  " + parname + " = " + value);
//								//OWLClass parcls = factory.getOWLClass(IRI.create(base + modelname + "_" + parname));
//								//OWLMethods.addClass(ontology, parcls, rxnparclass, manager);
//								OWLMethods.setClsDatatypeProperty(ontology, parcls.getIRI().toString(), base + "hasNumericalValue", value, manager);
//								if(GOclass!=null){
//									OWLMethods.setClsObjectProperty(ontology, parcls, GOclass, base + "hasReactionAnnotation", base + "annotationForParameter", false, manager);
//									OWLMethods.setClsObjectProperty(ontology, parcls, rxnclass, base + "hasAssociatedReaction", base + "hasReactionParameter", true, manager);
//								}
//								//OWLMethods.setClsObjectProperty(ontology, parcls.getIRI().toString(), object, rel, invrel, manager)
//								OWLMethods.setRDFLabel(ontology, parcls, parname, manager);
//								numpars++;
//							}
//						}
//						else{System.out.println("No parameters for kineticLaw listed");}
						
					}
				}
				else{System.out.println("No reactions in model " + modelname);}
			}
			double pcntprocessed = (100*i)/SBMLmodels.length;
			SBMLreactionFinder.msgbar.setValue((int) pcntprocessed);
			SBMLreactionFinder.msgarea.setText("Building local knowledge base..." + (int) pcntprocessed +"% complete");
		}
		logfilewriter.flush();
		logfilewriter.close();
	//	if(outputfile.exists()){outputfile.delete();}
	//	outputfile.createNewFile();
		manager.saveOntology(ontology,new RDFXMLOntologyFormat(),IRI.create(SBMLreactionFinder.rxnsrcfile.toURI()));
		SBMLreactionFinder.finder.initialize();
		System.out.println(numannotatedrxns + " annotated reactions");
		System.out.println(numunannotatedrxns + " unannotated reactions");
	}
	
	
	// Query BioPortal to get preferred label and synonyms for GO class
//	public static String[] queryBioPortalGOAndProcess(String GOclass){
//		ArrayList<String> labelandsynonyms = new ArrayList<String>();
//
//		URL resturl;
//		try {
//			resturl = new URL("http://data.bioontology.org/ontologies/GO/classes/http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2F" + GOclass);
//			System.out.println(resturl.toString());
//			
//	        String jsonstring = get(resturl.toString());
//	        
//	        System.out.println(jsonstring);
//	        
//	        JsonNode jsonnode = jsonToNode(jsonstring);
//	        
//	        if (!jsonnode.get("prefLabel").isNull()) {
//	        	labelandsynonyms.add(jsonnode.get("prefLabel").asText());
//	            
//	            for(JsonNode synnode : jsonnode.get("synonym")) {
//	            	labelandsynonyms.add(synnode.asText());
//	            }
//	        }
//	        
//	        // Print out all the labels
//	        for (String asdf : labelandsynonyms) {
//	            System.out.println(asdf);
//	        }
//		} catch (Exception e) {
//			logfilewriter.println(e.getMessage());
//		}
//		
//		return labelandsynonyms.toArray(new String[]{});
//	}
	
	
	public static String[] queryUniProtTaxonomyAndProcess(String id){
		// The first element in the returned array is the scientific name for the taxon
		// The other elements are other names for the taxon
		// The scientific name is used for the class name in the knowledge base
		String[] taxonandcommonnames = new String[]{""};
		SAXBuilder newbuilder = new SAXBuilder();
		Document docu = new Document();
		try {
			URL url = new URL("http://www.uniprot.org/taxonomy/"+id+".rdf");
			System.out.println(url.toString());
			URLConnection yc = url.openConnection();
			yc.setReadTimeout(60000); // Tiemout after a minute
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			docu = newbuilder.build(in);
			in.close();
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
			e.printStackTrace();
		}
		
		// Process XML results from UniProt REST service to get taxonomy info
		if (docu.hasRootElement()) {
			System.out.println(docu.getRootElement().toString());
			if (docu.getRootElement().getChild("Description",rdfns)!=null) {
				System.out.println(docu.getRootElement().getChild("Description",rdfns));
				Element descel = docu.getRootElement().getChild("Description",rdfns);
				String scientificname = "";
				if(descel.getChild("scientificName",uniprotns)!=null){
					scientificname = descel.getChild("scientificName",uniprotns).getText();
					if(!descel.getChildren("commonName",uniprotns).isEmpty()){
						Iterator<Element> otherit = descel.getChildren("commonName", uniprotns).iterator();
						taxonandcommonnames = new String[descel.getChildren("commonName", uniprotns).size()+1];
						taxonandcommonnames[0] = scientificname;
						int count = 1;
						while(otherit.hasNext()){
							taxonandcommonnames[count] = otherit.next().getText();
							count++;
						}
					}
					else{
						taxonandcommonnames = new String[]{scientificname};
					}
				}
			}
		}
		return taxonandcommonnames;
	}
	
	
	public static void setRDFLabelAndSynonyms(String[] labelandsyns, OWLClass GOclass, String annenc) throws OWLException{
		OWLMethods.setRDFLabel(ontology, GOclass, labelandsyns[0], manager);
		GOtermsadded.add(annenc);
		for(int u=1; u<labelandsyns.length; u++){
			OWLMethods.setClsDatatypeProperty(ontology, GOclass.getIRI().toString(), base + "synonym", labelandsyns[u], manager);
		}
	}
	
	
//	private static JsonNode jsonToNode(String json) {
//        JsonNode root = null;
//        try {
//            root = mapper.readTree(json);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return root;
//    }
//
//    private static String get(String urlToGet) {
//        URL url;
//        HttpURLConnection conn;
//        BufferedReader rd;
//        String line;
//        String result = "";
//        try {
//            url = new URL(urlToGet);
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
//            conn.setRequestProperty("Accept", "application/json");
//            rd = new BufferedReader( new InputStreamReader(conn.getInputStream()));
//            while ((line = rd.readLine()) != null) {
//                result += line;
//            }
//            rd.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
    
}
