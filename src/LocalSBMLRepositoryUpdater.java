import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import javax.xml.stream.XMLStreamException;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import uk.ac.ebi.biomodels.BioModelsWSClient;
import uk.ac.ebi.biomodels.BioModelsWSException;


public class LocalSBMLRepositoryUpdater {
	public BioModelsWSClient client;
	public PrintWriter writer;
	public LocalSBMLRepositoryUpdater() throws BioModelsWSException{
		client = new BioModelsWSClient();
		
	}
	
	
	public void update() throws BioModelsWSException, IOException, XMLStreamException{
		String[] idarray =  client.getAllCuratedModelsId();
		
		SBMLreactionFinder.msgbar.setIndeterminate(false);
		SBMLreactionFinder.msgarea.setText("Updating models...0% complete");		

		Arrays.sort(idarray);
		for(int x=0; x<idarray.length; x++){
//		for(int x=0; x<1; x++){
			File newfile = new File("resources/curated_models/" + idarray[x] + ".xml");
			writer = new PrintWriter(new FileWriter(newfile));
			System.out.println(idarray[x]);
			String modelstring = "";
			try {
				modelstring = client.getModelSBMLById(idarray[x]);
			} catch (Exception e) {e.printStackTrace();}
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newfile),"UTF8"));
			if(!modelstring.equals("")){
				Scanner scn = new Scanner(modelstring);
				while(scn.hasNextLine()){
					String nextline = scn.nextLine();
					bw.write(nextline);
					bw.newLine();
				}
				bw.flush();
				bw.close();
				scn.close();
			}
			else{
				System.out.println("Couldn't read data from model " + idarray[x]);
			}
			
			double pcntprocessed = (100*x)/idarray.length;
			SBMLreactionFinder.msgbar.setValue((int) pcntprocessed);
			SBMLreactionFinder.msgarea.setText("Updating models..." + (int) pcntprocessed + "% complete");		
		}
		try {
			SBMLreactionCollector.collectReactions();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OWLException e) {
			e.printStackTrace();
		}
		SBMLreactionFinder.msgbar.setIndeterminate(false);
		SBMLreactionFinder.msgbar.setValue(0);
		SBMLreactionFinder.msgarea.setText("Update complete");		
		SBMLreactionFinder.findbox.setEnabled(true);
		SBMLreactionFinder.searchbutton.setEnabled(true);
		SBMLreactionFinder.lastcacheupdate = SBMLreactionFinder.sdflog.format(new Date());
		SBMLreactionFinder.storeGUIsettings();
		System.out.println(SBMLreactionFinder.lastcacheupdate);
	}
}
