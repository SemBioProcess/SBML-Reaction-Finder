import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;


public class SearchResult extends JPanel implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Set<String> srcURIs;
	public Set<String> kinlaws;
	public JLabel GOlabel;
	public JLabel taxonlabel;
	public String GOstring;
	public String BiomodelsID;
	public String SBMLID;
	public Color color;
	public SearchResult(Set<String> srcURIs, String SBMLID, String GOstring, String srcmodelname, String taxon){
		this.srcURIs = srcURIs;
		this.GOstring = GOstring;
		this.SBMLID = SBMLID;
		GOlabel = new JLabel(GOstring);
		GOlabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		GOlabel.setForeground(Color.blue);
		GOlabel.setOpaque(false);
		GOlabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
		
		BiomodelsID = srcURIs.toArray(new String[]{})[0];
		//BiomodelsID = BiomodelsID.substring(BiomodelsID.lastIndexOf("/")+1,BiomodelsID.lastIndexOf(".xml"));
		setToolTipText(BiomodelsID);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(GOlabel);
		JLabel modellabel = new JLabel("Model: " + srcmodelname);
		modellabel.setOpaque(false);
		modellabel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		add(modellabel);
		
		String tax = taxon;
		if(taxon.equals(""))tax = "<unspecified>";
		
		taxonlabel = new JLabel("Taxon: " + tax);
		taxonlabel.setFont(new Font("SansSerif",Font.ITALIC,12));
		taxonlabel.setOpaque(false);
		taxonlabel.setBorder(BorderFactory.createEmptyBorder(0,3,3,3));
		this.add(taxonlabel);
		
		this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.setBackground(color);
		this.addMouseListener(this);
	}
	
	public void mouseClicked(MouseEvent arg0) {
		if((SBMLreactionFinder.currentsearchresult==null || SBMLreactionFinder.currentsearchresult!=this)){
			// NEED TO FIX THIS SO YOU CAN"T START TWO DOWNLOADS AT A TIME
				//&& (SBMLreactionFinder.progframe==null || !SBMLreactionFinder.progframe.isVisible())){
			
			if (arg0.getComponent() instanceof SearchResult) {
				// If we're going online to get the reaction data
				if(SBMLreactionFinder.useonlinemodels.isSelected()){
					SBMLreactionFinder.msgbar.setIndeterminate(true);
					SBMLreactionFinder.msgarea.setText("Downloading reaction data...");
					GenericThread task = new GenericThread(this, "showOnlineReactionData");
					task.start();
				}
				// Otherwise we're using the local model cache
				else{showLocalReactionData();}
			}
			if(SBMLreactionFinder.currentsearchresult!=null){
				SBMLreactionFinder.currentsearchresult.setBorder(BorderFactory.createEmptyBorder());
			}
			SBMLreactionFinder.currentsearchresult = this;
			SBMLreactionFinder.currentsearchresult.setBorder(BorderFactory.createLineBorder(Color.blue,3));
		}
	}
	public void mouseEntered(MouseEvent arg0) {
		this.setBackground(new Color(240, 150, 150));
	}
	public void mouseExited(MouseEvent arg0) {
		this.setBackground(Color.white);		
	}
	public void mousePressed(MouseEvent arg0) {
	}
	public void mouseReleased(MouseEvent arg0) {
	}
	
	
	public void showLocalReactionData(){
		File modelfile = new File("resources/curated_models/" + BiomodelsID + ".xml");
		
		try {
			ReactionView rv = new ReactionView(modelfile, SBMLID, BiomodelsID);
			SBMLreactionFinder.reactionpanelinside.removeAll();
			SBMLreactionFinder.reactionpanelinside.add(rv);
			rv.requestFocusInWindow();
		}
		catch (IOException | XMLStreamException e) {e.printStackTrace();}
		SBMLreactionFinder.reactionscroller.validate();
	}
	
	
	
	public void showOnlineReactionData(){
		try {
			ReactionView rv = new ReactionView(BiomodelsID, SBMLID, BiomodelsID);
			SBMLreactionFinder.reactionpanelinside.removeAll();
			SBMLreactionFinder.reactionpanelinside.add(rv);
		}
		catch (IOException | XMLStreamException e) {e.printStackTrace();}
		SBMLreactionFinder.reactionscroller.validate();
	}
	
}