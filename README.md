![SBML-Reaction-Finder](http://sbp.bhi.washington.edu/_/rsrc/1472691287695/projects/sbmlrxnfinder/Logo1small.png)

# SBML-Reaction-Finder
 A tool for discovering and reusing reactions from BioModels
 
 
## Overview

To provide a more modular approach to biological modeling, this software streamlines the retrieval and extraction of individual chemical reactions represented in the models of
 the BioModels repository. 

Reactions that match the search criteria are listed in the left scroll pane, and clicking on one of the search results brings up the reaction details. The reaction can then be extracted as a separate SBML model.


## Getting started

Double-click the SBMLreactionFinder.jar file. Please make sure you have Java 1.8 or higher installed.

To search, enter search terms in the search box and press "Search."  You can also use java regular expressions.


## Semantic aspects

To find reactions, the software leverages semantic annotations within SBML models, specifically Gene Ontology (GO), EC-codes, and  NCBI taxonomy terms.  The autosuggest feature in the search box shows the GO annotations applied to BioModels reactions. The BioModels curation team uses other types of annotations for reactions as well, such as KEGG, but currently the Reaction Finder only utilizes knowledge associated with GO or EC-code annotations. These other annotation types will be available in future releases. 


## Updating the knowledge base

To find reactions, the program searches a local knowledge base called BioModelsReactions.owl that comes with the software. 
Users can update this knowledge base at any time by selecting  "Update model local model cache" from the File menu. Using web services,  this will download all the latest versions of the curated models, then rebuild the local knowledge base from the updated cache. The entire update process takes several minutes.


## License

Version: MPL 1.1/GPL 2.0/LGPL 2.1

The contents of this file are subject to the Mozilla Public License Version 
1.1 (the "License"); you may not use this file except in compliance with 
the License. You may obtain a copy of the License at 
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is the SBML Reaction Finder.

The Initial Developer of the Original Code is
Maxwell Lewis Neal <mneal@uw.edu>.
Portions created by the Initial Developer are Copyright (C) 2011-2019.
the Initial Developer. All Rights Reserved.

Contributor(s):
	Herbert Sauro <hsauro@uw.edu>

Alternatively, the contents of this file may be used under the terms of
either the GNU General Public License Version 2 or later (the "GPL"), or
the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
in which case the provisions of the GPL or the LGPL are applicable instead
of those above. If you wish to allow use of your version of this file only
under the terms of either the GPL or the LGPL, and not to allow others to
use your version of this file under the terms of the MPL, indicate your
decision by deleting the provisions above and replace them with the notice
and other provisions required by the GPL or the LGPL. If you do not delete
the provisions above, a recipient may use your version of this file under
the terms of any one of the MPL, the GPL or the LGPL.
