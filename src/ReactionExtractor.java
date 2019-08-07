import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.jdom.Element;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;


public class ReactionExtractor {
	public Model model;
	public File outputfile;
	public static Date datenow;
	public String biomodelsID;
	public static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssZ");
	public ReactionExtractor(Model model, String biomodelsID){
		
		this.model = model;
		this.biomodelsID = biomodelsID;
	}
	
	public void extractAll(File outputdir) throws XMLStreamException, SBMLException, FileNotFoundException{
		for(int r=0; r<model.getNumReactions(); r++){
			Reaction rxn = model.getReaction(r);
			
			// Get unique name for reaction
			String reactionname = "";
			int rxnint = 1;
			if(!rxn.getName().equals("")){
				//System.out.println("has Name");
				reactionname = rxn.getName();
			}
			else if(!rxn.getId().equals("")){
				reactionname = rxn.getId();
			}
			else if(!rxn.getMetaId().equals("")){
				reactionname = rxn.getMetaId();
			}
			else{
				reactionname = "" + rxnint;
				rxnint++;
			}
			File outputf = new File(outputdir.getAbsolutePath() + "/" + model.getName() + "_" + reactionname + ".xml");
			Boolean check = checkoverwrite(outputf);
			if(check){
				extract(outputf, rxn.getId());
			}
		}
	}
	
	public Boolean checkoverwrite(File outputfile){
		Boolean oktosave = true;
		if (outputfile.exists()) {
			int overwriteval = JOptionPane.showConfirmDialog(null,
					"Overwrite " +  outputfile.getName() + "?", "Confirm overwrite",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (overwriteval != JOptionPane.OK_OPTION) {
				oktosave = false;
			}
		}
		return oktosave;
	}
	
	
	public String extract(File theoutputfile, String ID) throws XMLStreamException, SBMLException, FileNotFoundException{
		
//		newdoc.setLevelAndVersion(2, 1);
		Model newmodel = new SBMLDocument().createModel("TestModel"); //new Model();
		datenow = new Date();
		String timestamp = sdf.format(datenow);
		timestamp = timestamp.replace("-", "_");
		
		// Deal with namespaces, collect all from source model
		int level = model.getLevel();
		String versionnote = "";
		int version = model.getVersion();

		newmodel.getSBMLDocument().setLevelAndVersion(level, version);
		System.out.println(newmodel.getSBMLDocument().getLevel());
		System.out.println(newmodel.getSBMLDocument().getVersion());
		System.out.println(newmodel.getSBMLDocument().getDeclaredNamespaces());
		
		if(model.getSBMLDocument().getDeclaredNamespaces()!=null){
			
			for(String nsindex : model.getSBMLDocument().getDeclaredNamespaces().keySet()){
				
				if(! nsindex.equals("")
						&& ! model.getSBMLDocument().getDeclaredNamespaces().get(nsindex).equals("")){
					newmodel.getSBMLDocument().getDeclaredNamespaces().put(nsindex, model.getSBMLDocument().getDeclaredNamespaces().get(nsindex));
				}
			}
		}
		else{
			System.out.println("Namespaces were null");
		}
	
		newmodel.setId(model.getName().replace(" ", "_") + "_" + ID + "_" + timestamp);
		newmodel.setMetaId("metaid_" + timestamp);
		newmodel.setName(model.getName() + " " + ID + " " + timestamp);
		newmodel.setNotes("<body xmlns=\"http://www.w3.org/1999/xhtml\"> This is a single-reaction model that was auto-extracted from " + model.getName() + 
				" ("+ biomodelsID + ") " + " by the SBML Reaction Finder." + versionnote + "</body>");
	
		Model sbmlmodel = model;
		
		for(int l=0;l<model.getListOfUnitDefinitions().size();l++){
			System.out.println("unit def: " + model.getUnitDefinition(l).getId() + " " + model.getUnitDefinition(l).getName());
		}
		
		// Copy over all compartments in model (can't just extract the ones you need b/c 
		// sometimes a discarded compartment is used in the reaction's kinetic law
		// (see BIOMD...71 reaction "vpfk")
		for(int c=0; c<sbmlmodel.getListOfCompartments().size(); c++){
			newmodel = createCompartment(newmodel, sbmlmodel, sbmlmodel.getCompartment(c).getId());
		}
		
//		CVTerm cv1 = new CVTerm();
//		cv1.setQualifierType(libsbmlConstants.BIOLOGICAL_QUALIFIER);
//		cv1.setBiologicalQualifierType(libsbmlConstants.);
//		cv1.addResource("http://www.ebi.ac.uk/interpro/#IPR002394");
//		newmodel.addCVTerm(cv1); 
		
		
//		XMLNode taxonomynode = new XMLNode("something");
//		XMLNode onenode = new XMLNode("<one>");
//		onenode.insertChild(0,new XMLNode("two"));
//		taxonomynode.addChild(onenode);
//		XMLNode xml = model.getAnnotation();
//		Boolean cont = true;
//		XMLNode parent = model.getAnnotation();
//		
//		newmodel.setAnnotation(taxonomynode);
		//model.getAnnotation().getChild(0).getChild(0);
		
		// Copy over reactions
		ListOf<Reaction> rxnlist = sbmlmodel.getListOfReactions();
		for(int x=0; x<rxnlist.size(); x++){
			Reaction rxn = sbmlmodel.getReaction(x);
			
			if(rxn.getId().equals(ID)){
				// Copy over the information about the reaction
				Reaction newrxn = newmodel.createReaction();
				newrxn.setId(rxn.getId());
				newrxn.setFast(rxn.getFast());
				KineticLaw newkinlaw = newrxn.createKineticLaw();
				newkinlaw = createKineticLaw(newkinlaw, rxn.getKineticLaw(), newmodel);
				newrxn.setMetaId(rxn.getMetaId());
				newrxn.setName(rxn.getName());
				
				if(rxn.isSetNotes())
					newrxn.setNotes(rxn.getNotes());
				
//				newrxn.setNotes(rxn.getNotesString());
				
				newrxn.setReversible(rxn.getReversible());
				newrxn.setAnnotation(rxn.getAnnotation());
//				newrxn.setAnnotation(rxn.getAnnotationString());
				
				// Copy over info about the modifiers in the reaction
				for(int m=0; m<rxn.getListOfModifiers().size(); m++){
					ModifierSpeciesReference mod = rxn.getModifier(m);
					ModifierSpeciesReference newmod = newrxn.createModifier();
					newmod.setAnnotation(mod.getAnnotation());
					newmod.setId(mod.getId());
					newmod.setMetaId(mod.getMetaId());
					newmod.setName(mod.getName());
					
					if(mod.isSetNotes())
						newmod.setNotes(mod.getNotes());
					
//					newmod.setNotes(mod.getNotesString());
					
					processSBOterm(newmodel, mod, newmod);
					
					newmod.setSpecies(mod.getSpecies());
					if(!mod.getSpecies().equals("")){
						newmodel = createSpecies(newmodel, sbmlmodel, sbmlmodel.getSpecies(mod.getSpecies()));
					}
				}
				// Copy over info about the reactants
				for(int r=0; r<rxn.getListOfReactants().size(); r++){
					SpeciesReference reactant = rxn.getReactant(r);
					SpeciesReference newreactant = newrxn.createReactant();
					newreactant.setAnnotation(reactant.getAnnotation());
//					newreactant.setAnnotation(reactant.getAnnotationString());
					newreactant.setId(reactant.getId());
					newreactant.setDenominator(reactant.getDenominator());
					newreactant.setMetaId(reactant.getMetaId());
					newreactant.setName(reactant.getName());
					
					if(reactant.isSetNotes())
						newreactant.setNotes(reactant.getNotes());
//					newreactant.setNotes(reactant.getNotesString());
					
					processSBOterm(newmodel, reactant, newreactant);
					
					newreactant.setSpecies(reactant.getSpecies());
					if(!reactant.getSpecies().equals("")){
						newmodel = createSpecies(newmodel, sbmlmodel, sbmlmodel.getSpecies(reactant.getSpecies()));
					}
					newreactant.setStoichiometry(reactant.getStoichiometry());
					newreactant.setStoichiometryMath(reactant.getStoichiometryMath());
				}
				// Copy over info about the products
				for(int p=0; p<rxn.getListOfProducts().size(); p++){
					SpeciesReference product = rxn.getProduct(p);
					SpeciesReference newproduct = newrxn.createProduct();
					newproduct.setAnnotation(product.getAnnotation());
//					newproduct.setAnnotation(product.getAnnotationString());
					newproduct.setId(product.getId());
					newproduct.setDenominator(product.getDenominator());
					newproduct.setMetaId(product.getMetaId());
					newproduct.setName(product.getName());
					
					if(product.isSetNotes())
						newproduct.setNotes(product.getNotes());
//					newproduct.setNotes(product.getNotesString());
					
					processSBOterm(newmodel, product, newproduct);
					
					newproduct.setSpecies(product.getSpecies());
					if(!product.getSpecies().equals("")){
						newmodel = createSpecies(newmodel, sbmlmodel, sbmlmodel.getSpecies(product.getSpecies()));
					}
					newproduct.setStoichiometry(product.getStoichiometry());
					newproduct.setStoichiometryMath(product.getStoichiometryMath());
				}
				
				// Copy over the info about the global parameters
				for(int pr=0; pr<sbmlmodel.getListOfParameters().size(); pr++){
					Parameter par = sbmlmodel.getParameter(pr);
					Parameter newpar = newmodel.createParameter();
					newpar.setAnnotation(par.getAnnotation());
//					newpar.setAnnotation(par.getAnnotationString());
					newpar.setConstant(par.getConstant());
					newpar.setId(par.getId());
					newpar.setMetaId(par.getMetaId());
					newpar.setName(par.getName());
					
					if(par.isSetNotes())
						newpar.setNotes(par.getNotes());
//					newpar.setNotes(par.getNotesString());
					
					processSBOterm(newmodel, par, newpar);
					
					if(par.isSetUnits()) {
						newmodel = createUnitDefinition(newmodel, sbmlmodel, par.getDerivedUnitDefinition());
						newpar.setUnits(par.getUnits());
					}
					
					
//					if(!newpar.getUnits().equals("")){
//						//System.out.println("Global par " + newpar.getId() + " has units " + newpar.getUnits());
//						if(newpar.getDerivedUnitDefinition()!=null && !newpar.getDerivedUnitDefinition().getId().equals("")){
//							newmodel = createUnitDefinition(newmodel, sbmlmodel, newpar.getDerivedUnitDefinition());
//						}
//						else{
//							if(sbmlmodel.getUnitDefinition(newpar.getUnits())!=null){
//								newmodel = createUnitDefinition(newmodel, sbmlmodel, sbmlmodel.getUnitDefinition(newpar.getUnits()));
//							}
//							else{
//								System.out.println(newpar.getId() + " had a null derived unit definition");
//							}
//						}
//					}
					
					newpar.setValue(par.getValue());
				}
				
				// Copy over all user-defined functions 
				for(int fd=0; fd<sbmlmodel.getListOfFunctionDefinitions().size(); fd++){
					FunctionDefinition funcdef = sbmlmodel.getFunctionDefinition(fd);
					String funcid = funcdef.getId();
					if(rxn.getKineticLaw().getFormula().contains(funcid)){
						FunctionDefinition newfuncdef = newmodel.createFunctionDefinition();
						newfuncdef.setAnnotation(funcdef.getAnnotation());
//						newfuncdef.setAnnotation(funcdef.getAnnotationString());
						newfuncdef.setId(funcdef.getId());
						newfuncdef.setMath(funcdef.getMath());
						newfuncdef.setMetaId(funcdef.getMetaId());
						newfuncdef.setName(funcdef.getName());
						
						if(funcdef.isSetNotes())
							newfuncdef.setNotes(funcdef.getNotes());
//						newfuncdef.setNotes(funcdef.getNotesString());
						
						processSBOterm(newmodel, funcdef, newfuncdef);
					}
				}
				
				// Copy over kinetic law parameters
				for(int kpr=0; kpr<rxn.getKineticLaw().getNumParameters(); kpr++){
					LocalParameter kpar = rxn.getKineticLaw().getParameter(kpr);
					LocalParameter newkpar = newkinlaw.createLocalParameter();
					newkpar.setAnnotation(kpar.getAnnotation());
//					newkpar.setAnnotation(kpar.getAnnotationString());
					//newkpar.setExplicitlyConstant(kpar.getConstant());
					newkpar.setId(kpar.getId());
					newkpar.setMetaId(kpar.getMetaId());
					newkpar.setName(kpar.getName());
					
					if(kpar.isSetNotes())
						newkpar.setNotes(kpar.getNotes());
					
//					newkpar.setNotes(kpar.getNotesString());
					
					processSBOterm(newmodel, kpar, newkpar);
					
					if(kpar.isSetUnits()) {
						newmodel = createUnitDefinition(newmodel, sbmlmodel, kpar.getDerivedUnitDefinition());
						newkpar.setUnits(kpar.getUnits());
					}
					
					
//					
//					if(!kpar.getUnits().equals("")){
//						//System.out.println("Kinetic par " + kpar.getId() + " has units " + kpar.getUnits());
//						if(kpar.getDerivedUnitDefinition()!=null && !kpar.getDerivedUnitDefinition().getId().equals("")){
//							newmodel = createUnitDefinition(newmodel, sbmlmodel, kpar.getDerivedUnitDefinition());
//						}
//						else{
//							if(sbmlmodel.getUnitDefinition(kpar.getUnits())!=null){
//								newmodel = createUnitDefinition(newmodel, sbmlmodel, sbmlmodel.getUnitDefinition(kpar.getUnits()));
//							}
//							else{
//								System.out.println(kpar.getId() + " had a null derived unit definition");
//							}
//						}
//					}
					newkpar.setValue(kpar.getValue());
				}
			}
		}
		SBMLWriter writer = new SBMLWriter();
		
		//newmodel.getSBMLDocument().setLevelAndVersion(2, 1);
		if(theoutputfile!=null){
			writer.writeSBMLToFile(newmodel.getSBMLDocument(), theoutputfile.getAbsolutePath());
		}
		return writer.writeSBMLToString(newmodel.getSBMLDocument());
	}
	
	public Model createSpecies(Model newmodel, Model origmodel, Species origsp) throws XMLStreamException{
		if(newmodel.getSpecies(origsp.getId())==null){
			Species newsp = newmodel.createSpecies();
			newsp.setAnnotation(origsp.getAnnotation());
//			newsp.setAnnotation(origsp.getAnnotationString());
			newsp.setBoundaryCondition(origsp.getBoundaryCondition());
			newsp.setCharge(origsp.getCharge());
			newsp.setCompartment(origsp.getCompartment());
			newsp.setConstant(origsp.getConstant());
			newsp.setHasOnlySubstanceUnits(origsp.getHasOnlySubstanceUnits());
			newsp.setId(origsp.getId());
			newsp.setInitialAmount(origsp.getInitialAmount());
			newsp.setInitialConcentration(origsp.getInitialConcentration());
			newsp.setMetaId(origsp.getMetaId());
			newsp.setName(origsp.getName());
			
			if(origsp.isSetNotes())
				newsp.setNotes(origsp.getNotes());
			
//			newsp.setNotes(origsp.getNotes());
//			newsp.setNotes(origsp.getNotesString());
			
			processSBOterm(newmodel, origsp, newsp);
			
			newsp.setSpatialSizeUnits(origsp.getSpatialSizeUnits());
			
			if(newmodel.getLevel()==2 && newmodel.getVersion()>=2 && newmodel.getVersion()<=4)
				newsp.setSpeciesType(origsp.getSpeciesType());
			
			newsp.setSubstanceUnits(origsp.getSubstanceUnits());
			
			if(origsp.isSetUnits()) {
				newmodel = createUnitDefinition(newmodel, origmodel, origsp.getDerivedUnitDefinition());
				newsp.setUnits(origsp.getUnits());
			}
			
//			newsp.setUnits(origsp.getUnits());
//			if(!origsp.getUnits().equals("")){
//				if(origsp.getDerivedUnitDefinition()!=null && !origsp.getDerivedUnitDefinition().getId().equals("")){
//					newmodel = createUnitDefinition(newmodel, origmodel, origsp.getDerivedUnitDefinition());
//				}
//				else{
//					if(origmodel.getUnitDefinition(origsp.getUnits())!=null){
//						newmodel = createUnitDefinition(newmodel, origmodel, origmodel.getUnitDefinition(origsp.getUnits()));
//					}
//					else{
//						System.out.println(origsp.getId() + " had a null derived unit definition");
//					}
//				}
//			}
			
			if( ! origsp.getCompartment().equals("")){
				createCompartment(newmodel, origmodel, origsp.getCompartment());
			}
		}
		return newmodel;
	}
	
	
	public Model createCompartment(Model newmodel, Model origmodel, String origcpstring) throws XMLStreamException{
		System.out.println("Creating compartment " + origcpstring);
		Boolean cont = true;
		String cpname = origcpstring;
		while(cont){
			Compartment cp = origmodel.getCompartment(cpname);
			// Check if compartment already added
			if(newmodel.getCompartment(cpname)==null){
				Compartment newcp = newmodel.createCompartment();
				newcp = setCompartmentData(newcp, cp, newmodel, origmodel);
				if(!newcp.getOutside().equals("")){
					cpname = newcp.getOutside();
				}
				else{
					cont=false;
				}
			}
			else{
				cont=false;
			}
		}
		return newmodel;
	}
	
	public Compartment setCompartmentData(Compartment newcp, Compartment cp, Model newmodel, Model origmodel) throws XMLStreamException{
		newcp.setAnnotation(cp.getAnnotation());
		newcp.setCompartmentType(cp.getCompartmentType());
		newcp.setConstant(cp.getConstant());
		newcp.setId(cp.getId());
		newcp.setMetaId(cp.getMetaId());
		newcp.setName(cp.getName());
		
		if(cp.isSetNotes())
			newcp.setNotes(cp.getNotes());
		
//		newcp.setNotes(cp.getNotes());
//		newcp.setNotes(cp.getNotesString());
		
		newcp.setOutside(cp.getOutside());
		
		processSBOterm(newmodel, cp, newcp);
		
		newcp.setSize(cp.getSize());
		newcp.setSpatialDimensions(cp.getSpatialDimensions());
		
		if(cp.isSetUnits()) {
			newmodel = createUnitDefinition(newmodel, origmodel, cp.getDerivedUnitDefinition());
			newcp.setUnits(cp.getUnits());
		}
		
//		if(!cp.getUnits().equals("")){
//			if(cp.getDerivedUnitDefinition()!=null && !cp.getDerivedUnitDefinition().getId().equals("")){
//				System.out.println("Compartment " + cp.getId() + " has UnitDefinition " + cp.getDerivedUnitDefinition().getId());
//				newmodel = createUnitDefinition(newmodel, origmodel, cp.getDerivedUnitDefinition());
//			}
//			else{
//				if(origmodel.getUnitDefinition(cp.getUnits())!=null){
//					newmodel = createUnitDefinition(newmodel, origmodel, origmodel.getUnitDefinition(cp.getUnits()));
//				}
//				else{
//					System.out.println(cp.getId() + " had a null derived unit definition");
//				}
//			}
//		}
		//newcp.setVolume(cp.getVolume());
		return newcp;
	}
	
	
	
	public Model createUnitDefinition(Model newmodel, Model origmodel, UnitDefinition ud) throws XMLStreamException{
		// Break this up - test for non-null ud first
		if(!ud.getId().equals("") && newmodel.getUnitDefinition(ud.getId())==null){
			UnitDefinition newud = newmodel.createUnitDefinition();
			//System.out.println("Creating new units " + ud.getId());
			newud.setAnnotation(ud.getAnnotation());
			//newud.setAnnotation(ud.getAnnotationString());
			newud.setId(ud.getId());
			
			if(ud.isSetMetaId())
				newud.setMetaId(ud.getMetaId());
			
			newud.setName(ud.getName());
			
			if(ud.isSetNotes())
				newud.setNotes(ud.getNotes());
			
//			newud.setNotes(ud.getNotesString());
			
			processSBOterm(newmodel, ud, newud);
			
			for(int u=0; u<ud.getListOfUnits().size(); u++){
				newmodel = createUnits(newmodel, newud, ud, u);
			}
		}
		return newmodel;
	}
	
	public Model createUnits(Model newmodel, UnitDefinition newud, UnitDefinition ud, int idnum){
		Unit newun = newud.createUnit();
		Unit un = ud.getUnit(idnum);
		newun.setAnnotation(un.getAnnotation());
		//newun.setAnnotation(un.getAnnotationString());
		newun.setExponent(un.getExponent());
		//newun.setId(un.getId());
		newun.setKind(un.getKind());
		
		if(un.isSetMetaId())
			newun.setMetaId(un.getMetaId());
		
		newun.setMultiplier(un.getMultiplier());
		//newun.setName(un.getName());
		
		if(un.isSetNotes())
			newun.setNotes(un.getNotes());
		
		//newun.setNotes(un.getNotesString());
		
		if(newmodel.getLevel()==2 && newmodel.getVersion()==1)
			newun.setOffset(un.getOffset());
		
		processSBOterm(newmodel, un, newun);
		
		newun.setScale(un.getScale());
		return newmodel;
	}
	
	
	
	public KineticLaw createKineticLaw(KineticLaw newlaw, KineticLaw oldlaw, Model newmodel){
		newlaw.setAnnotation(oldlaw.getAnnotation());
		//newlaw.setAnnotation(oldlaw.getAnnotationString());
		//newlaw.setFormula(oldlaw.getFormula());
		//newlaw.setId(oldlaw.getId());
		newlaw.setMath(oldlaw.getMath());
		newlaw.setMetaId(oldlaw.getMetaId());
		//newlaw.setName(oldlaw.getName());
		
		if(oldlaw.isSetNotes())
			newlaw.setNotes(oldlaw.getNotes());
		
		//newlaw.setNotes(oldlaw.getNotesString());
		
		processSBOterm(newmodel, oldlaw, newlaw);
		
		if(oldlaw.isSetSubstanceUnits())
			newlaw.setSubstanceUnits(oldlaw.getSubstanceUnits());
		
		if(oldlaw.isSetTimeUnits())
			newlaw.setTimeUnits(oldlaw.getTimeUnits());
		
		return newlaw;
	}
	
	
	
	// ------------------- Helper methods -------------------
	
	public void processSBOterm(Model newmodel, SBase oldsb, SBase newsb) {
		if(newmodel.getLevel()>=2) {
			if(newmodel.getLevel()==2 && newmodel.getVersion()>=3 && oldsb.isSetSBOTerm()) {
				if(oldsb.getSBOTerm()>=0 && oldsb.getSBOTerm()<=9999999)
					newsb.setSBOTerm(oldsb.getSBOTerm());
			}
		}
	}
}
