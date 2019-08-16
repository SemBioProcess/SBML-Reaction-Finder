import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ebi.biomodels.BioModelsWSException;


public class SBMLreactionFinder extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static SBMLreactionFinder finder;
	public String version = "2.0.0";
	public URI ReactionCollectionURL = URI.create("http://www.bhi.washington.edu/research/SemBioProcess/SBML/BioModelsReactions.owl");
	public static File rxnsrcfile = new File("resources/BioModelsReactions.owl");
	public static Set<String> taxons = new HashSet<String>();
	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLOntology rxnont;
	public static OWLDataFactory factory = manager.getOWLDataFactory();
	public static String base = "http://www.bhi.washington.edu/BioModelsReactions#";
	public static Set<OWLClass> matchingclasses = new HashSet<OWLClass>();
	public static int initxpos = 0;
	public static int initypos = 0;
	public static int initwidth = 600;
	public static int initheight = 700;
	public static String lastcacheupdate = "";
	public static GenericThread loadingtask;
	public static LocalSBMLRepositoryUpdater up;
	public static SimpleDateFormat sdflog = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
	public static SBMLreactionFinder frame;
	public static JPanel searchpanel = new JPanel();
	public static JPanel searchtoppanel;
	public static String[] taxonarray = new String[]{};
	public static JComboBox<String> taxoncombo;
	public static JPanel limitsearchpanel = new JPanel();
	public static JPanel resultspanel = new JPanel();
	public static JPanel reactionviewer = new JPanel();
	public static JComboBox<String> findbox;
	public static JPanel panelinside = new JPanel();
	public static JPanel reactionpanelinside = new JPanel();
	public static JScrollPane resultsscroller;
	public static JScrollPane reactionscroller;
	public static JSplitPane sp;
	public static JPanel msgpanel = new JPanel();
	public static JProgressBar msgbar;
	public static JTextArea msgarea;
	public static JLabel searchlabel;
	public static JButton searchbutton;
	public static JLabel resultsnumber = new JLabel();
	public static File currentdirectory = new File("");
	public static SearchResult currentsearchresult;
	public static List<String> autocompletelist = new ArrayList<String>();
	public static JTextField tf;
	public static ComboKeyHandler ckh;
	public static String GObase = "http://purl.org/obo/owl/GO#";
	
	public static Hashtable<String, String[]> startSettingsTable;
	public String title;
	
	public static JMenuBar menubar;
	public static JMenu filemenu;
	public static JMenuItem fileitemquit;
	public static JMenu optionsmenu;
	public static ButtonGroup cachegroup;
	public static JRadioButtonMenuItem uselocalmodels;
	public static JRadioButtonMenuItem useonlinemodels;
	public static JMenuItem updatemodelcache;
	public static JMenu helpmenu;
	public static JMenuItem helpitemabout;
	
	public static String searchexp = "";
	
	public int maskkey;
	
	public static boolean LINUXorUNIX;
	public static boolean WINDOWS;
	public static boolean MACOSX;

	
	public SBMLreactionFinder(String title){
		super("OSXAdapter");
		
		MACOSX = OSValidator.isMac();
		WINDOWS = OSValidator.isWindows();
		LINUXorUNIX = OSValidator.isUnix();
		maskkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		registerForMacOSXEvents();

		this.title = title;
		this.setTitle(title);
	}
	
	
	public void setUpGUI() throws FileNotFoundException, BioModelsWSException{
		startSettingsTable = createHashtableFromFile(new File("resources/cfg"));
		initxpos = Integer.parseInt(startSettingsTable.get("XstartPosition")[0].trim());
		initypos = Integer.parseInt(startSettingsTable.get("YstartPosition")[0].trim());
		initwidth = Integer.parseInt(startSettingsTable.get("startWidth")[0].trim());
		initheight = Integer.parseInt(startSettingsTable.get("startHeight")[0].trim());
		if(startSettingsTable.get("startDirectory")[0].equals("000")){
			currentdirectory.equals(System.getProperty("user.home"));
		}
		else{
			currentdirectory = new File(startSettingsTable.get("startDirectory")[0]);
		}
		lastcacheupdate = startSettingsTable.get("modelCacheLastUpdated")[0].trim();
		
		setBackground(new Color(207, 215, 252));
		setPreferredSize(new Dimension(initwidth, initheight));
		setLocation(initxpos, initypos);
		
		// set up the threads
		up = new LocalSBMLRepositoryUpdater();
		loadingtask = new GenericThread(finder, "initialize");
		
		this.setLayout(new BorderLayout());
		
		searchlabel = new JLabel("Enter search term");
		 
		findbox = new JComboBox<String>(new String[]{});
		//findbox.setBounds(50,50,100,21);
		findbox.setPreferredSize(new Dimension(300,30));
		findbox.setForeground(Color.blue);
		findbox.setEnabled(false);
		
		taxoncombo = new JComboBox<String>();
		searchbutton = new JButton("Search");
		searchbutton.setEnabled(false);
		
		searchtoppanel = new JPanel();
		
		searchtoppanel.add(searchlabel);
		searchtoppanel.add(findbox);
		searchtoppanel.add(searchbutton);
		//searchtoppanel.add(Box.createHorizontalGlue());
		
		JPanel limitpanel = new JPanel();
		JLabel limitlabel = new JLabel("Limit search by taxon: ");
		limitpanel.add(limitlabel);
		limitpanel.add(taxoncombo);
		
		searchpanel.setLayout(new BorderLayout());
		searchpanel.add(searchtoppanel,BorderLayout.NORTH);
		searchpanel.add(limitpanel,BorderLayout.SOUTH);
		
		panelinside.setLayout(new BoxLayout(panelinside,BoxLayout.Y_AXIS));
		panelinside.setBackground(Color.white);
		reactionpanelinside.setLayout(new BoxLayout(reactionpanelinside,BoxLayout.Y_AXIS));
		reactionpanelinside.setBackground(Color.white);
		
		resultsscroller = new JScrollPane(panelinside);
		resultsscroller.getVerticalScrollBar().setUnitIncrement(9);
		reactionscroller = new JScrollPane(reactionpanelinside);
		reactionscroller.getVerticalScrollBar().setUnitIncrement(9);
		reactionscroller.setBackground(Color.white);
		
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsscroller, reactionscroller);
		sp.setResizeWeight(0.5);
		sp.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		resultsnumber.setText("0 reactions found");
		resultsnumber.setBackground(Color.white);
		
		msgbar = new JProgressBar();
		msgbar.setVisible(true);
		msgbar.setPreferredSize(new Dimension(300,25));
		msgbar.setMaximumSize(new Dimension(999999,30));
//		msgbar.set
	
		msgarea = new JTextArea();
		msgarea.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
		msgarea.setFont(new Font("SansSerif", Font.ITALIC, 11));
		msgarea.setEditable(false);
		msgarea.setOpaque(false);
		
		msgpanel.setLayout(new BorderLayout());
		msgpanel.add(msgbar, BorderLayout.NORTH);
		msgpanel.add(msgarea, BorderLayout.SOUTH);
		
		menubar = new JMenuBar();
		filemenu = new JMenu("File");
		fileitemquit = new JMenuItem("Quit");
		fileitemquit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,maskkey));
		fileitemquit.addActionListener(this);
		updatemodelcache = new JMenuItem("Update local model cache");
		updatemodelcache.addActionListener(this);
		filemenu.add(updatemodelcache);
		filemenu.add(new JSeparator());
		filemenu.add(fileitemquit);
		
		optionsmenu = new JMenu("Options");
		cachegroup = new ButtonGroup();
		uselocalmodels = new JRadioButtonMenuItem("Use local model cache");
		useonlinemodels = new JRadioButtonMenuItem("Use online model cache");
		cachegroup.add(uselocalmodels);
		cachegroup.add(useonlinemodels);
		uselocalmodels.setSelected(true);
		
		optionsmenu.add(uselocalmodels);
		optionsmenu.add(useonlinemodels);
		
		helpmenu = new JMenu("Help");
		helpitemabout = new JMenuItem("About");
		helpitemabout.addActionListener(this);
		helpmenu.add(helpitemabout);
		
		menubar.add(filemenu);
		menubar.add(optionsmenu);
		menubar.add(helpmenu);
		
		this.setJMenuBar(menubar);
		
		this.add(searchpanel, BorderLayout.NORTH);
		this.add(sp, BorderLayout.CENTER);
		this.add(msgpanel, BorderLayout.SOUTH);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		msgbar.setIndeterminate(true);
		msgarea.setText("Loading resources");
		loadingtask.start();
	}
		
	
	public void initialize() throws OWLOntologyCreationException{
		loadReactionKnowledgeBase();
		
        findbox.setEditable(true);
		findbox.setEnabled(true);
		if(findbox.getActionListeners().length==0){
			findbox.addActionListener(this);
		}
		
		searchbutton.setEnabled(true);
		searchbutton.removeActionListener(this);
		searchbutton.addActionListener(this);
		
		tf.removeActionListener(this);
		tf.addActionListener(this);

		tf.addKeyListener(ckh);

		msgbar.setIndeterminate(false);
		msgbar.setValue(0);
		msgarea.setText("Ready");
	}
	
	
	public void loadReactionKnowledgeBase(){
		if(manager.getOntologies().contains(rxnont)){
			manager.removeOntology(rxnont);
		}
		try {
			rxnont = manager.loadOntologyFromOntologyDocument(rxnsrcfile);
		} catch (OWLOntologyCreationException e) {e.printStackTrace();}
		
		autocompletelist = new ArrayList<String>();
		autocompletelist.clear();
		taxons = new HashSet<String>();
		taxons.add("<All taxons>");
		// Populate the autocomplete list
		OWLClass GOparent = factory.getOWLClass(IRI.create(base + "GeneOntologyReferenceConcept"));
		OWLClass taxonparent = factory.getOWLClass(IRI.create(base + "Taxon"));
		
		for(OWLClassExpression GOcls : GOparent.getSubClasses(rxnont)){
			Set<String> synonyms = OWLMethods.getClsValueDataProperties(rxnont, GOcls.asOWLClass().getIRI().toString(), base + "synonym");
			if(!OWLMethods.getRDFLabels(rxnont, GOcls.asOWLClass())[0].equals("")){
				autocompletelist.add(OWLMethods.getRDFLabels(rxnont, GOcls.asOWLClass())[0]);
			}
			for(String synonym : synonyms){
				synonym = synonym.trim();
				if(!synonym.equals("")){
					autocompletelist.add(synonym);
				}
			}
		}
		
		// Get the associated organisms
		for(OWLClassExpression cls : taxonparent.getSubClasses(rxnont)){
			if(!OWLMethods.getRDFLabels(rxnont, cls.asOWLClass())[0].equals("")){
				taxons.add(OWLMethods.getRDFLabels(rxnont, cls.asOWLClass())[0]);
			}
		}
		
		findbox.removeAllItems();
		String[] annarray = (String[]) autocompletelist.toArray(new String[]{});
		Arrays.sort(annarray);
		for(int x=0; x<annarray.length; x++){
			String item = annarray[x];
			//item = item.replaceAll("\\(",Pattern.quote("\\*"));
			//item = item.replaceAll(Pattern.quote(")"),Pattern.quote("\\)"));
			findbox.addItem(item);
		}
		findbox.setSelectedIndex(-1);
		
		taxoncombo.removeAllItems();
		taxonarray = taxons.toArray(new String[]{});
		Arrays.sort(taxonarray);
		for(int t=0;t<taxonarray.length;t++){taxoncombo.addItem(taxonarray[t]);}
		taxoncombo.setSelectedIndex(0);
		findbox.requestFocusInWindow();
		tf = (JTextField)findbox.getEditor().getEditorComponent();
		tf.removeKeyListener(ckh);
		ckh = new ComboKeyHandler(findbox);
	}

	
	
	public static void search() throws OWLOntologyCreationException{
		reactionpanelinside.removeAll();
		resultsscroller.validate();
		resultsscroller.repaint();
		
		msgarea.setText("Searching...0% complete");
		if(!searchexp.equals("")){
			searchexp = searchexp.replaceAll(Pattern.quote(" | "), "|");
			searchexp = searchexp.toLowerCase();
			searchexp = searchexp.replaceAll(" or ", "|");
			searchexp = searchexp.replaceAll(" and ", ".*");
			searchexp = searchexp.replaceAll(" ", ".*");
			
			//String[] splitstring = searchexp.split("and");
			//String[] splitstring = new String[]{searchexp};
			
			int numresults = 0;
			panelinside.removeAll();
			// reset the search results
			matchingclasses = new HashSet<OWLClass>();
			OWLClass GOparent = factory.getOWLClass(IRI.create(base + "GeneOntologyReferenceConcept"));
			OWLClass unannparent = factory.getOWLClass(IRI.create(base + "UnannotatedReaction"));
			double totclasses = unannparent.getSubClasses(rxnont).size() + GOparent.getSubClasses(rxnont).size();
			double remainingclasses = totclasses;
			
			
			try{
				Pattern pattern = Pattern.compile(searchexp);
				// Search the unannotated reactions first (ids and names)
				
				for(OWLClassExpression unanncls : unannparent.getSubClasses(rxnont)){
					String srcmodel = OWLMethods.getClsValueObjectProperty(rxnont, unanncls.asOWLClass().getIRI().toString(), base + "hasSourceModel").toArray(new String[]{})[0];
					String stringtosearch = "";
					//stringtosearch = stringtosearch + OWLMethods.getRDFLabels(rxnont, factory.getOWLClass(IRI.create(srcmodel)))[0] + " ";
					//System.out.println(stringtosearch);
//					if(!OWLMethods.getClsValueDataProperties(rxnont, srcmodel, base + "hasNotes").isEmpty()){
//						stringtosearch = OWLMethods.getClsValueDataProperties(rxnont, srcmodel, base + "hasNotes").toArray(new String[]{})[0] + " ";
//					}
					Set<String> idsandnames = new HashSet<String>();
					idsandnames.addAll(OWLMethods.getClsValueDataProperties(rxnont, unanncls.asOWLClass().getIRI().toString(), base + "hasName"));
					idsandnames.addAll(OWLMethods.getClsValueDataProperties(rxnont, unanncls.asOWLClass().getIRI().toString(), base + "hasId"));
					//idsandnames.add(unanncls.asOWLClass().getIRI().getFragment());
					
					String taxonomy = "";
					for(String text : idsandnames){stringtosearch = stringtosearch + text + " ";}
					if(!OWLMethods.getClsValueObjectProperty(rxnont, srcmodel, base + "occursInOrganism").isEmpty()){
						String taxuri = OWLMethods.getClsValueObjectProperty(rxnont, srcmodel, base + "occursInOrganism").toArray(new String[]{})[0];
						taxonomy = OWLMethods.getRDFLabels(rxnont, factory.getOWLClass(IRI.create(taxuri)))[0];
						//stringtosearch = stringtosearch + taxonomy + " ";
						Set<String> othernames = OWLMethods.getClsValueDataProperties(rxnont, taxuri, base + "hasOtherName");
						for(String othername : othernames){
							stringtosearch = stringtosearch + othername + " ";
						}
					}
					
					//System.out.println(stringtosearch);
//					if(!OWLMethods.getClsValueDataProperties(rxnont, unanncls.asOWLClass().getIRI().toString(), base + "occursInOrganism").isEmpty()){
//						taxonomy = OWLMethods.getClsValueDataProperties(rxnont, unanncls.asOWLClass().getIRI().toString(), base + "occursInOrganism").toArray(new String[]{})[0];
//					}
					Boolean match = false;
					if(taxoncombo.getSelectedIndex()>0){
						String selectedtaxon = (String)taxoncombo.getSelectedItem();
						if(taxonomy.equals(selectedtaxon)){
							Matcher m = pattern.matcher(stringtosearch.toLowerCase().trim());
							if(m.find()){
								match = true;
							}
						}
					}
					else{
						Matcher m = pattern.matcher(stringtosearch.toLowerCase().trim());
						if(m.find()){
							match = true;
						}
					}
					if(match){
						matchingclasses.add(unanncls.asOWLClass());
						//System.out.println(stringtosearch);
						}
					
					remainingclasses = remainingclasses -1;
					msgbar.setValue((int) (100*(totclasses-remainingclasses)/totclasses));
					msgarea.setText("Searching..." + (int) (100*(totclasses-remainingclasses)/totclasses) + "% complete");
				}
				
				// Next, search the reactions annotated against GO terms
				for(OWLClassExpression GOcls : GOparent.getSubClasses(rxnont)){
					String basestringtosearch = "";
					basestringtosearch = OWLMethods.getRDFLabels(rxnont, GOcls.asOWLClass())[0].toLowerCase() + " ";
					
					Set<String> synonyms = OWLMethods.getClsValueDataProperties(rxnont, GOcls.asOWLClass().getIRI().toString(), base + "synonym");
					for(String synonym : synonyms){
						basestringtosearch = basestringtosearch + synonym + " ";
					}
					for(OWLClassExpression reaction : GOcls.asOWLClass().getSubClasses(rxnont)){
						String srcmodel = "";
						String stringtosearch = basestringtosearch;
						if(!OWLMethods.getClsValueObjectProperty(rxnont, reaction.asOWLClass().getIRI().toString(), base + "hasSourceModel").isEmpty()){
							srcmodel = OWLMethods.getClsValueObjectProperty(rxnont, reaction.asOWLClass().getIRI().toString(), base + "hasSourceModel").toArray(new String[]{})[0];
							//stringtosearch = stringtosearch + OWLMethods.getRDFLabels(rxnont, factory.getOWLClass(IRI.create(srcmodel)))[0] + " ";
//							if(!OWLMethods.getClsValueDataProperties(rxnont, srcmodel, base + "hasNotes").isEmpty()){
//								basestringtosearch = basestringtosearch + OWLMethods.getClsValueDataProperties(rxnont, srcmodel, base + "hasNotes").toArray(new String[]{})[0] + " ";
//							}
						}
						else{
							System.out.println(reaction.toString() + " does not have an associated source model");
						}

						Set<String> idsandnames = new HashSet<String>();
						idsandnames.addAll(OWLMethods.getClsValueDataProperties(rxnont, reaction.asOWLClass().getIRI().toString(), base + "hasName"));
						idsandnames.addAll(OWLMethods.getClsValueDataProperties(rxnont, reaction.asOWLClass().getIRI().toString(), base + "hasId"));
						//idsandnames.add(reaction.asOWLClass().getIRI().getFragment());
						
						String extratext = "";
						for(String text : idsandnames){extratext = extratext + text + " ";}
						String taxonomy = "";
						for(String text : idsandnames){stringtosearch = stringtosearch + text + " ";}
//						if(!OWLMethods.getClsValueDataProperties(rxnont, reaction.asOWLClass().getIRI().toString(), base + "occursInOrganism").isEmpty()){
//							taxonomy = OWLMethods.getClsValueDataProperties(rxnont, reaction.asOWLClass().getIRI().toString(), base + "occursInOrganism").toArray(new String[]{})[0];
//						}
						if(!srcmodel.equals("")
								&& !OWLMethods.getClsValueObjectProperty(rxnont, srcmodel, base + "occursInOrganism").isEmpty()){
							String taxuri = OWLMethods.getClsValueObjectProperty(rxnont, srcmodel, base + "occursInOrganism").toArray(new String[]{})[0];
							taxonomy = OWLMethods.getRDFLabels(rxnont, factory.getOWLClass(IRI.create(taxuri)))[0];
							//stringtosearch = stringtosearch + taxonomy + " ";
							Set<String> othernames = OWLMethods.getClsValueDataProperties(rxnont, taxuri, base + "hasOtherName");
							for(String othername : othernames){
								stringtosearch = stringtosearch + othername + " ";
							}
						}
						
						Boolean match = false;

						if(taxoncombo.getSelectedIndex()>0){
							String selectedtaxon = (String)taxoncombo.getSelectedItem();
							if(taxonomy.equals(selectedtaxon)){
								Matcher m = pattern.matcher(stringtosearch.toLowerCase().trim());
								if(m.find()){
									match = true;
								}
							}
						}
						else{
							Matcher m = pattern.matcher(stringtosearch.toLowerCase().trim());
							if(m.find()){
								match = true;
							}
						}
						if(match){
							matchingclasses.add(reaction.asOWLClass());
						}
					}					
					remainingclasses = remainingclasses -1;
					msgbar.setValue((int) (100*(totclasses-remainingclasses)/totclasses));
					msgarea.setText("Searching..." + (int) (100*(totclasses-remainingclasses)/totclasses) + "% complete");
				}
			}
			catch(PatternSyntaxException ex){JOptionPane.showMessageDialog(null, "Could not parse search text as regular expression.\nPlease edit and try again");}
			
			msgbar.setValue(0);
			
			msgarea.setText("Collecting data on matching reactions...0% complete");
			totclasses = matchingclasses.size();
			int maxsize = 500;
			Boolean truncate = false;
			if(totclasses>maxsize){totclasses=maxsize;truncate = true;}
			remainingclasses = totclasses;
			int counter = 0;
			
			// Collect all reactions within the matching classes and list all the subclassed reactions
			for(OWLClassExpression onerxn : matchingclasses){
				counter++;
				if(counter>maxsize && truncate){break;}
				String SBMLID = "";
				String srcmodelname = "";
				String GOstring = "";
				String taxonomy = "";
				Set<String> srcURLs = new HashSet<String>();
				
				srcURLs = OWLMethods.getClsValueDataProperties(rxnont, onerxn.asOWLClass().getIRI().toString(), base + "hasSourceModelURL");
				SBMLID = OWLMethods.getRDFLabels(rxnont, onerxn.asOWLClass())[0];
				srcmodelname = onerxn.asOWLClass().getIRI().getFragment();
				srcmodelname = srcmodelname.replace(SBMLID,"");
				srcmodelname = srcmodelname.replace("_", " ");
				srcmodelname.trim();
				GOstring = "";
				for(OWLClassExpression supercls : onerxn.asOWLClass().getSuperClasses(rxnont)){
					if(GOparent.getSubClasses(rxnont).contains(supercls)){
						GOstring = OWLMethods.getRDFLabels(rxnont, supercls.asOWLClass())[0];
					}
				}
				
				String srcmod = OWLMethods.getClsValueObjectProperty(rxnont, onerxn.asOWLClass().getIRI().toString(), base + "hasSourceModel").toArray(new String[]{})[0];
				srcURLs = OWLMethods.getClsValueDataProperties(rxnont, srcmod, base + "hasBiomodelsID");
				if(!OWLMethods.getClsValueObjectProperty(rxnont, srcmod, base + "occursInOrganism").isEmpty()){
					String taxuri = OWLMethods.getClsValueObjectProperty(rxnont, srcmod, base + "occursInOrganism").toArray(new String[]{})[0];
					taxonomy = OWLMethods.getRDFLabels(rxnont, factory.getOWLClass(IRI.create(taxuri)))[0];
				}
				  
				SearchResult sr = new SearchResult(srcURLs, SBMLID, GOstring, srcmodelname, taxonomy);
				panelinside.add(sr);
				panelinside.add(new JSeparator());
				remainingclasses = remainingclasses - 1;
				msgbar.setValue((int) (100*(totclasses-remainingclasses)/totclasses));
				msgarea.setText("Collecting data on matching reactions..." + (int) (100*(totclasses-remainingclasses)/totclasses) + "% complete");
			}
			panelinside.add(Box.createVerticalGlue());
			
			if(matchingclasses.isEmpty()){
				JTextField noresults = new JTextField("No matches");
				noresults.setFont(new Font("SansSerif", Font.ITALIC, 12));
				noresults.setMaximumSize(new Dimension(999999, 30));
				noresults.setBorder(BorderFactory.createEmptyBorder());
				noresults.setEditable(false);
				panelinside.add(noresults);
				msgarea.setText("0 results");
			}
			else if(truncate){
				msgarea.setText(maxsize + "+ results");
			}
			else{
				msgarea.setText(matchingclasses.size() + " results");
			}
			resultsnumber.setText(numresults + " reactions found");
			resultsscroller.validate();
			resultsscroller.repaint();
		}
		msgbar.setIndeterminate(false); msgbar.setValue(0);
	}
	

	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		
		if( o == findbox){
		}
		if( o == searchbutton || o == tf){
			if(!tf.getText().equals("")){
				ckh.shouldHide = true;
				searchexp = tf.getText();
				msgbar.setIndeterminate(false);
				GenericThread searchtask = new GenericThread(this, "search");
				searchtask.start();
			}
		}
		if(o == fileitemquit){quit();System.exit(0);}
		
		if(o == updatemodelcache){
			int confirm = JOptionPane.showConfirmDialog(this, "Updating the model cache can take several minutes,\n" +
					"depending on the speed of your internet connection.\n\nLast update was on " + lastcacheupdate + "\n\nProceed?",
					"Confirm update", JOptionPane.YES_NO_OPTION);
			if(confirm==JOptionPane.OK_OPTION){
				findbox.setEnabled(false);
				searchbutton.setEnabled(false);
				msgbar.setIndeterminate(true);
				msgarea.setText("Connecting to Biomodels.net...");
				GenericThread updatetask = new GenericThread(up, "update");
				updatetask.start(); 
			}
		}
		
		if(o == helpitemabout) JOptionPane.showMessageDialog(null, "SBML Reaction Finder\nVersion " + version
				+ "\nSpecification: Herbert Sauro\nImplementation: Maxwell Lewis Neal\nUniversity of Washington, USA\n" +
						"Contact mneal@uw.edu", "About",
				JOptionPane.PLAIN_MESSAGE);
		}

	
	public static Hashtable<String, String[]> createHashtableFromFile(
			File readfile) throws FileNotFoundException {
		Hashtable<String, String[]> table = new Hashtable<String, String[]>();
		Scanner unitsfilescanner = new Scanner(readfile);
		String nextline = "";
		String key = "";
		Set<String> values = new HashSet<String>();

		if (!readfile.exists()) {
			msgarea.setText("Could not create hashtable from file: "
					+ readfile.getAbsolutePath());
		} else {
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			while (unitsfilescanner.hasNext()) {
				values.clear();
				nextline = unitsfilescanner.nextLine();
				semiseparatorindex = nextline.indexOf(";");
				key = nextline.substring(0, semiseparatorindex);
				nextline = nextline.substring(semiseparatorindex + 2,
						nextline.length());
				Boolean repeat = true;
				while (repeat) {
					if (nextline.contains(",")) {
						commaseparatorindex = nextline.indexOf(",");
						values.add(nextline.substring(0, nextline.indexOf(",")));
						commaseparatorindex = nextline.indexOf(",");
						nextline = nextline.substring(commaseparatorindex + 2,
								nextline.length());
					} else {
						values.add(nextline);
						repeat = false;
					}
				}
				table.put(key, (String[]) values.toArray(new String[] {}));
			}
			unitsfilescanner.close();
		}
		return table;
	}
	
	
	
	// Generic registration with the Mac OS X application menu
	// Checks the platform, then attempts to register with the Apple EAWT
	// See OSXAdapter.java to see how this is done without directly referencing
	// any Apple APIs
	public void registerForMacOSXEvents() {
		if (MACOSX) {
			try {
				// Generate and register the OSXAdapter, passing it a hash of
				// all the methods we wish to
				// use as delegates for various
				// com.apple.eawt.ApplicationListener methods
				OSXAdapter.setQuitHandler(this,
						getClass().getDeclaredMethod("quit", (Class[]) null));

				// OSXAdapter.setAboutHandler(this,
				// getClass().getDeclaredMethod("about", (Class[])null));
				// OSXAdapter.setPreferencesHandler(this,
				// getClass().getDeclaredMethod("preferences", (Class[])null));
				// OSXAdapter.setFileHandler(this,
				// getClass().getDeclaredMethod("loadImageFile", new Class[] {
				// String.class }));
			} catch (Exception e) {
				System.err.println("Error while loading the OSXAdapter:");
				e.printStackTrace();
			}
		}
	}
	
	
	public static boolean quit() {
		// Store GUI settings on close
		storeGUIsettings();
		return true;
	}
	
	
	public static void storeGUIsettings(){
		int endx = finder.getLocation().x;
		int endy = finder.getLocation().y;
		int endheight = finder.getHeight();
		int endwidth = finder.getWidth();
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(new File("resources/cfg")));
			writer.println("XstartPosition; " + endx);
			writer.println("YstartPosition; " + endy);
			writer.println("startHeight; " + endheight);
			writer.println("startWidth; " + endwidth);
			writer.println("startDirectory; "+ currentdirectory.getAbsolutePath());
			writer.println("modelCacheLastUpdated; " + lastcacheupdate);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws OWLOntologyCreationException, MalformedURLException, URISyntaxException, FileNotFoundException, BioModelsWSException {
		try {
			
			if(OSValidator.isWindows()){
				try {
				    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				        if ("Nimbus".equals(info.getName())) {
				            UIManager.setLookAndFeel(info.getClassName());
				            break;
				        }
				    }
				} catch (UnsupportedLookAndFeelException e) {
				} catch (ClassNotFoundException e) {
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				}
			}
			else{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
			
		} catch (Exception e) {
			e.printStackTrace();}
		
		finder = new SBMLreactionFinder("SBML Reaction Finder");
		finder.setUpGUI();
	}
}
