package umlToOwlPlugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.Writer;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;

public class UmlToOwlAction extends MDAction {
	private static final long serialVersionUID = 1L;
	
	private OWLOntology myOntology;
	private OutputStream os;
	
	public UmlToOwlAction(String id, String name) {
		super(id, name, null, null);
	}
	
	public void actionPerformed(ActionEvent e) {
		// Gaunamas aktyvus projektas
		Project p = Application.getInstance().getProjectsManager().getActiveProject();
		if (p != null) {
			// Dialoge atvaizduojamas projekto pavadinimas
//			JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Model: " + p.getModel().getName());
			// Gaunamas nested package
			Collection<Package> nestedPackage = p.getPrimaryModel().getNestedPackage();
			boolean packageExist = false;
			String[] ontology = {};
			// Ciklas per visus paketus (package)
			for (Iterator<Package> iterator = nestedPackage.iterator(); iterator.hasNext();) {
				Package package1 = (Package) iterator.next();
				// Jeigu paketas turi stereotipa - "OWL2 ontology"
				if (StereotypesHelper.isElementStereotypedBy(package1, "OWL2 ontology"))
				{	
					/**
					 * IRI of current ontology
					 * 
					 * getTagValue params (e, sterotypeName, tagName)
					 */
//					@SuppressWarnings("unused")
					String ontoIRI=getTagValue(package1, "OWL2 ontology", "ontology IRI");
					packageExist = true;
					/* Debug Start */
					JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Package name: " + package1.getName());
					JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "This is:" + package1.getHumanName());
					/* Debug End */
					Collection<Element> ownedElement = package1.getOwnedElement();
					for (Iterator<Element> iterator2 = ownedElement.iterator(); iterator2.hasNext();) {
						Element element = (Element) iterator2.next();
						if (element instanceof Class)
						{
							/**
							 * push classes to array. @TODO - naudoti koki nors liba sitam
							 * 
							 * push params (array, what to push)
							 */
							ontology = push(ontology, ((Class) element).getName());
							/**
							 * Debug start
							 */
							OWLOntologyManager m = OWLManager.createOWLOntologyManager();
							//JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "OWLOntologyManager: " + m);
							IRI iri = IRI.create(ontoIRI);
							//JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Ontologijos IRI: " + iri);
							try {
								myOntology = m.createOntology(iri);

								//File file = new File("/tmp/owlApiTest.owl");
								//m.saveOntology(myOntology);
								//JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Ontologija getClass getName: " + myOntology.getClass().getName());
								//JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Ontologija: " + myOntology);
							} catch(OWLOntologyCreationException Ex) {
								JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Exception: " + Ex);
							}
							
							//File file = new File("/tmp/owlApiTest.owl");
							try {
								os = new FileOutputStream(new File("owlApiTest.owl"));
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}
							try {
								//m.saveOntology(myOntology, iri);
								m.saveOntology(myOntology, new OWLXMLOntologyFormat(), os);
								JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Successfully safed");
							} catch(OWLOntologyStorageException EStorage) {
								JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Exception: " + EStorage);
							}
							
							JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "This is class IRI: " + getTagValue(element, "OWL2Entity","EntityIRI"));
							/**
							 * Debug End
							 */
							exportClass((Class) element);
							exportClassAttributes((Class) element);
						}
						else if (element instanceof Association)
						{
							
						}
					}
				}
			}
			if (packageExist != true) {
				JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Can not find package with specified stereotype");
			}
			/**
			 * 
			 */
			writeToFile(ontology);
		} else {
			JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Open project first!");
		}		
	}
	
	private void exportClass(Class c1) {
		/**
		 * @todo
		 */
//		getTagValue(c1, sterotypeName, tagName)
	}
	
	private void exportClassAttributes(Class c1) {
		@SuppressWarnings("unused")
		List<Property> attribute = c1.getAttribute();
		
	}
	
	@SuppressWarnings("unused")
	private boolean isSubclass(Class c1) {
		Collection<Classifier> general = c1.getGeneral();
		if (general.isEmpty()) {
			return false;
		}
		
		return true;
	}
	
	private String getTagValue(Element e, String sterotypeName, String tagName) {
		String result = "";
		// find a profile
		Project project = Project.getProject(e);
		Profile profile = StereotypesHelper.getProfile(project, "OWL 2 profile");
		// find a stereotype
		Stereotype stereotype = StereotypesHelper.getStereotype(project, sterotypeName, profile);
		if (!StereotypesHelper.isElementStereotypedBy(e, sterotypeName)) {
			return "nera stereotipo";
		}
		@SuppressWarnings("rawtypes")
		List value = StereotypesHelper.getStereotypePropertyValue(e, stereotype, tagName);
		for (int k = 0; k < value.size(); ++k)
		{
			// a tag value
			result = (String) value.get(k);
			continue;
		}
		return result;
	}
	
	/**
	 * Push to array
	 * 
	 * @param array
	 * @param push
	 * @return
	 */
	private static String[] push(String[] array, String push) {
	    String[] longer = new String[array.length + 1];
	    for (int i = 0; i < array.length; i++)
	        longer[i] = array[i];
	    longer[array.length] = push;
	    return longer;
	}
	
	public static void writeToFile(String[] data) {
        Writer writer = null;

        try {
            File file = new File("ontology.owl");
            writer = new BufferedWriter(new FileWriter(file));
            if (data.length == 0 || data == null) {
            	writer.write("No content...");
            } else {
            	writer.write("Prefix(owl:=<http://www.w3.org/2002/07/owl#>)" + System.lineSeparator());
        		writer.write("Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)" + System.lineSeparator());
        		writer.write("Prefix(xml:=<http://www.w3.org/XML/1998/namespace>)" + System.lineSeparator());
        		writer.write("Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)" + System.lineSeparator());
        		writer.write("Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)" + System.lineSeparator());
        		writer.write(System.lineSeparator() + System.lineSeparator());
        		
        		writer.write("Ontology(<http://isd.ktu.lt/ontologies/Agents>" + System.lineSeparator());
        		writer.write(System.lineSeparator());
        		
        		for (String className : data) {
        			writer.write("Declaration(Class(<http://isd.ktu.lt/ontologies/Agents#" + className + ">))" + System.lineSeparator());
        		}
        		writer.write(")");
            }
            
        } catch (FileNotFoundException e) {
        	JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "File not found Exception");
            e.printStackTrace();
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "IO Exception");
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            	JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "IO Exception");
                e.printStackTrace();
            }
        }
        JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "File successfully generated in MD installation directory!");
    }
}
