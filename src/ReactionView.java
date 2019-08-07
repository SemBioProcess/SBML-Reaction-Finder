import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SpeciesReference;

import uk.ac.ebi.biomodels.BioModelsWSClient;
import uk.ac.ebi.biomodels.BioModelsWSException;


public class ReactionView extends JPanel implements ActionListener, MouseListener{
	public Object modelidentifier;
	public String rxnid;
	public String biomodID;
	public SBMLDocument sbmldoc;
	public JButton getrxnbutton = new JButton("Save reaction as SBML");
	public JButton getallrxnsbutton = new JButton("Save all model reactions");
	public JPanel buttonpanel = new JPanel();
	public JTextArea reactioninfoarea;
	public Model model;
	public BioModelsWSClient client;
	
	public ReactionView(Object modelidentifier, String rxnid, String biomodID) throws IOException, XMLStreamException{
		this.rxnid = rxnid;
		this.biomodID = biomodID;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.white);
		if(modelidentifier instanceof String){
			client = new BioModelsWSClient();
			sbmldoc = loadSBMLmodel((String) modelidentifier);
		}
		else if(modelidentifier instanceof File){
			sbmldoc = loadSBMLmodel((File) modelidentifier);
		}
		
		if(sbmldoc!=null && sbmldoc.getModel()!=null){
			getallrxnsbutton.setText("Save all " + sbmldoc.getModel().getListOfReactions().size() + " model reactions");

			model = sbmldoc.getModel();
			Reaction rxn = null;
			for(int r=0; r<model.getListOfReactions().size(); r++){
				Reaction reaction = model.getReaction(r);
				if(reaction.getId().equals(rxnid)){
					rxn = reaction;
				}
			}
			String rxnareastr = "";
			if(!rxn.getName().equals("")){
				//System.out.println("has Name");
				rxnareastr = "Reaction: " + rxn.getName();
			}
			else if(!rxn.getId().equals("")){
				rxnareastr = "Reaction: " + rxn.getId();
			}
			else{
				rxnareastr = "Reaction: " + rxnid + "(unnamed in BioModels)";
			}
			JTextArea rxnarea = new JTextArea(rxnareastr);
			rxnarea.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
			rxnarea.setEditable(false);
			rxnarea.setFont(new Font("SansSerif", Font.BOLD, 14));
			this.add(rxnarea);
			
			getrxnbutton.addActionListener(this);
			getallrxnsbutton.addActionListener(this);
			buttonpanel.setBackground(Color.white);
			buttonpanel.setMaximumSize(new Dimension(600, 500));
			buttonpanel.add(getrxnbutton);
			buttonpanel.add(getallrxnsbutton);
			this.add(buttonpanel);
			
			reactioninfoarea = new JTextArea();
			reactioninfoarea.addMouseListener(this);
			reactioninfoarea.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
			reactioninfoarea.setEditable(false);
			reactioninfoarea.setLineWrap(true);
			reactioninfoarea.append("Reactants:\n");
			for(int sp=0; sp<rxn.getNumReactants(); sp++){
				SpeciesReference reactantref = rxn.getReactant(sp);
				String reactantname = "";
				String reactantid = "";
				if(!model.getSpecies(reactantref.getSpecies()).getName().equals("")){
					reactantname = model.getSpecies(reactantref.getSpecies()).getName();
				}
				if(!model.getSpecies(reactantref.getSpecies()).getId().equals("")){
					reactantid = model.getSpecies(reactantref.getSpecies()).getId();
				}
				
				if(!reactantname.equals("") && !reactantid.equals("")){
					reactioninfoarea.append("    " + reactantname + " (" + reactantid + ")");
				}
				else if(!reactantid.equals("")){
					reactioninfoarea.append("    " + reactantid);
				}
				else if(!reactantname.equals("")){
					reactioninfoarea.append("    " + reactantname);
				}
				else{}
				
				if(reactantref.getStoichiometry()>1){
					reactioninfoarea.append("    [X" + reactantref.getStoichiometry() + "]");
				}
				reactioninfoarea.append("\n");
			}
			
			reactioninfoarea.append("\nProducts:\n");
			for(int sp=0; sp<rxn.getNumProducts(); sp++){
				SpeciesReference product = rxn.getProduct(sp);
				String productname = "";
				String productid = "";
				if(!model.getSpecies(product.getSpecies()).getName().equals("")){
					productname = model.getSpecies(product.getSpecies()).getName();
				}
				if(!model.getSpecies(product.getSpecies()).getId().equals("")){
					productid = model.getSpecies(product.getSpecies()).getId();
				}
				
				if(!productname.equals("") && !productid.equals("")){
					reactioninfoarea.append("    " + productname + " (" + productid + ")");
				}
				else if(!productname.equals("")){
					reactioninfoarea.append("    " + productname);
				}
				else if(!productid.equals("")){
					reactioninfoarea.append("    " + productid);
				}
				else{}
				if(product.getStoichiometry()>1){
					reactioninfoarea.append("    [X" + product.getStoichiometry() + "]");
				}
				reactioninfoarea.append("\n");
			}
			
			reactioninfoarea.append("\nModifiers:\n");
			for(int mod=0; mod<rxn.getNumModifiers(); mod++){
				ModifierSpeciesReference modifier = rxn.getModifier(mod);
				String modname = "";
				String modid = "";
				if(!model.getSpecies(modifier.getSpecies()).getName().equals("")){
					modname = model.getSpecies(modifier.getSpecies()).getName();
				}
				if(!model.getSpecies(modifier.getSpecies()).getId().equals("")){
					modid = model.getSpecies(modifier.getSpecies()).getId();
				}
				
				if(!modname.equals("") && !modid.equals("")){
					reactioninfoarea.append("    " + modname + " (" + modid + ")");
				}
				else if(!modname.equals("")){
					reactioninfoarea.append("    " + modname);
				}
				else if(!modid.equals("")){
					reactioninfoarea.append("    " + modid);
				}
				else{}
				reactioninfoarea.append("\n");
			}
			reactioninfoarea.append("\nKinetic Law:\n" + "    " + rxn.getKineticLaw().getFormula() + "\n");

			// Add in the local parameter info
			reactioninfoarea.append("\nLocal (reaction-specific) parameter values:\n");
			for(int par=0; par<rxn.getKineticLaw().getNumParameters(); par++){
				KineticLaw kl = rxn.getKineticLaw();
				LocalParameter param = kl.getParameter(par);
				reactioninfoarea.append("    " + param.getId() + " = " + param.getValue() + " " + param.getUnits() + "\n"); 
			}
			
			// Add in the global parameter info
			reactioninfoarea.append("\nGlobal parameter values:\n");

			for(int par=0; par<model.getNumParameters(); par++){
				Parameter param = model.getParameter(par);
				reactioninfoarea.append("    " + param.getId() + " = " + param.getValue() + " " + param.getUnits() + "\n"); 
			}
			
			// Add the compartment info
			reactioninfoarea.append("\nModel compartments:\n");
			for(int comp=0; comp<model.getNumCompartments(); comp++){
				Compartment cpt = model.getCompartment(comp);
				reactioninfoarea.append("    " + cpt.getId() + " = " + cpt.getSize() + " " + cpt.getUnits() + "\n");
			}
			
			reactioninfoarea.setCaretPosition(0);
			this.add(reactioninfoarea);			
		}
	}
	
	
	public SBMLDocument loadSBMLmodel(File loc) throws IOException, XMLStreamException{
		SBMLDocument newdoc = new SBMLReader().readSBML(loc.getAbsolutePath());
		return newdoc;
	}
	
	
	public SBMLDocument loadSBMLmodel(String id) throws IOException{
		//System.out.println(id);
		SBMLDocument newdoc = null;
		try {
			newdoc = new SBMLReader().readSBMLFromString(client.getModelSBMLById(id));
		} catch (BioModelsWSException e) {
			JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
		} catch (XMLStreamException e) {
			  JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
		}
		
	//	String sbmlmodelstring = "";
//		if (url.toString().startsWith("http://")) {
//			System.out.println(url.toString());
//
//			Boolean online = true;
//			
//			HttpURLConnection.setFollowRedirects(false);
//			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
//			httpcon.setRequestMethod("HEAD");
//			try {
//				httpcon.getResponseCode();
//			} catch (Exception e) {
//				JOptionPane.showMessageDialog(this.getParent(), "Server response not detected - please make sure you are connected to the net");
//				online = false;
//			}
//			if (online) {
//				URLConnection urlcon = url.openConnection();
//				urlcon.setDoInput(true);
//				urlcon.setUseCaches(false);
//				urlcon.setReadTimeout(60000);
//				BufferedReader d = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
//				String s;
//				int size = httpcon.getContentLength();
//				int sizeremaining = size;
//				while ((s = d.readLine()) != null) {
//					double pcntprocessed = 100*(size-sizeremaining)/size;
//					sbmlmodelstring = sbmlmodelstring + s + "\n";
//					sizeremaining = sizeremaining-s.getBytes().length;
//					SBMLreactionFinder.msgbar.setValue((int) pcntprocessed);
//					SBMLreactionFinder.msgarea.setText("Downloading reaction data..." + (int) pcntprocessed + "% complete");
//				}
//				d.close();
//			} 
//		}
//		try {
//			newdoc = new SBMLReader().readSBMLFromString(sbmlmodelstring);
//		} catch (Exception e) {e.printStackTrace();}
		
		SBMLreactionFinder.msgbar.setIndeterminate(false); SBMLreactionFinder.msgbar.setValue(0);
		SBMLreactionFinder.msgarea.setText("Finished downloading");
		return newdoc;
	}

	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if(o == getrxnbutton){
			ReactionExtractor re = new ReactionExtractor(model, biomodID);
			File savefile = chooseSaveLocation();
			if(savefile!=null){
				Boolean checkoverwrite = re.checkoverwrite(savefile);
				if(savefile!=null && checkoverwrite){
					try {
						re.extract(savefile, rxnid);
					} catch (SBMLException e) {
						JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
					} catch (XMLStreamException e) {
						JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
					}
					SBMLreactionFinder.msgarea.setText("Saved " + savefile.getName());
				}
			}
		}
		if(o == getallrxnsbutton){
			ReactionExtractor re = new ReactionExtractor(model, biomodID);
			File savefolder = chooseSaveLocationFolder();
			if(savefolder!=null){
				try {
					re.extractAll(savefolder);
				} catch (SBMLException e) {
					JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
				} catch (XMLStreamException e) {
					JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
				}
				SBMLreactionFinder.msgarea.setText("Reactions saved in " + savefolder.getAbsolutePath());
			}
		}
	}
	
	
	public File chooseSaveLocation(){
		JFileChooser filec = new JFileChooser();
		Boolean saveok = false;
		File outputfile = null;
		while (!saveok) {
			filec.setCurrentDirectory(SBMLreactionFinder.currentdirectory);
			filec.setDialogTitle("Choose location to save SemSim file");
			filec.addChoosableFileFilter(new FileFilter(new String[] { "xml" }));
			int returnVal = filec.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				outputfile = new File(filec.getSelectedFile().getAbsolutePath());
				SBMLreactionFinder.currentdirectory = filec.getCurrentDirectory();
				if (!outputfile.getAbsolutePath().endsWith(".xml")
						&& !outputfile.getAbsolutePath().endsWith(".xml")) {
					outputfile = new File(filec.getSelectedFile().getAbsolutePath()
							+ ".xml");
				} else {}
				saveok = true;
			} else {
				saveok = true;
				outputfile = null;
			}
		}
		return outputfile;
	}
	
	
	public File chooseSaveLocationFolder(){
		JFileChooser filec = new JFileChooser();
		File outputdir = null;
		filec.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		filec.setCurrentDirectory(SBMLreactionFinder.currentdirectory);
		filec.setDialogTitle("Choose directory to save the separate SBML models");
		int returnVal = filec.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			outputdir = new File(filec.getSelectedFile().getAbsolutePath());
			SBMLreactionFinder.currentdirectory = filec.getCurrentDirectory();
		}
		return outputdir;
	}
	
	
	
	class PopUp extends JPopupMenu implements ActionListener, ClipboardOwner {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public JMenuItem copyItem;
	    public JMenuItem copySBML;
	    public JMenuItem copyAll;
	    public JTextArea ta;
	    public PopUp(JTextArea ta){
	    	this.ta = ta;
	        copyItem = new JMenuItem("Copy selected text");
	        copyAll = new JMenuItem("Copy all text");
	        copySBML = new JMenuItem("Copy as SBML");
	        copyItem.addActionListener(this);
	        copySBML.addActionListener(this);
	        copyAll.addActionListener(this);
	        add(copyItem);
	        add(copyAll);
	        add(copySBML);
	    }
		public void actionPerformed(ActionEvent arg0) {
			if(arg0.getSource()==copyItem){
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    clipboard.setContents(new StringSelection(ta.getSelectedText()), this );
			}
			else if(arg0.getSource() == copyAll){
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    clipboard.setContents(new StringSelection(ta.getText()), this );
			}
			else if(arg0.getSource()==copySBML){
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				ReactionExtractor re = new ReactionExtractor(model, biomodID);
			    try {
					clipboard.setContents(new StringSelection(re.extract(null, rxnid)), this );
				} catch (SBMLException e) {
					JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
				} catch (XMLStreamException e) {
					JOptionPane.showMessageDialog(SBMLreactionFinder.finder, e.getMessage());
				}
			}
		}
		public void lostOwnership(Clipboard arg0, Transferable arg1) {
			// TODO Auto-generated method stub
			
		}
	}

	


	public void mouseClicked(MouseEvent arg0) {
		arg0.consume();
		//System.out.println(arg0.getModifiersEx());
		if(arg0.getModifiers()==InputEvent.BUTTON3_MASK | arg0.getModifiersEx()==128){
			PopUp menu = new PopUp(reactioninfoarea);
			//menu.
	        menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		}
//		switch(arg0.getModifiers()) {
//	      case InputEvent.BUTTON1_MASK: {
//	        System.out.println("That's the LEFT button");     
//	        break;
//	        }
//	      case InputEvent.BUTTON2_MASK: {
//	        System.out.println("That's the MIDDLE button");     
//	        break;
//	        }
//	      case InputEvent.BUTTON3_MASK: {
//	        System.out.println("That's the RIGHT button");
//	        
//
//	        break;
//	        }
//		}
	}


	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
