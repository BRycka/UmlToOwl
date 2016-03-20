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

import umlToOwlPlugin.OwlAPI;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;

@SuppressWarnings("unused")
public class UmlToOwlAction extends MDAction {
	private static final long serialVersionUID = 1L;
	private OwlAPI OwlApi;

	/**
	 * 
	 * @param id
	 * @param name
	 */
	public UmlToOwlAction(String id, String name) {
		super(id, name, null, null);
	}
	
	public void actionPerformed(ActionEvent e) {
		Project p = Application.getInstance().getProjectsManager().getActiveProject(); // get active project
		if (p != null) {
			/* Debug */ //JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Model: " + p.getPrimaryModel().getName());
			Collection<Package> nestedPackage = p.getPrimaryModel().getNestedPackage(); // get collection of all packages in the project
			boolean packageExist = false; // by default package does not exist
			for (Iterator<Package> iterator = nestedPackage.iterator(); iterator.hasNext();) { // loop through all packages
				Package package1 = (Package) iterator.next();
				if (StereotypesHelper.isElementStereotypedBy(package1, Stereotypes.OWL_ONTOLOGY))
				{	
					/* Debug */ //JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Package name: " + package1.getName());
					packageExist = true; // package found so set packageExist as true
					String ontoIRI = getTagValue(package1, Stereotypes.OWL_ONTOLOGY, "ontology IRI"); // get IRI of current package
					try {
						OwlApi = new OwlAPI(ontoIRI, Constants.PATH_SAVE_TO);
					} catch (OWLOntologyCreationException e1) {
						/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Error while creating OwlAPI object");
					}
					
					Collection<Element> ownedElement = package1.getOwnedElement(); // get collection of current package owned elements
					for (Iterator<Element> iterator2 = ownedElement.iterator(); iterator2.hasNext();) { // loop through all owned elements
						Element element = (Element) iterator2.next();
						if (element instanceof Class)
						{
							/* Debug */ //JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "This is class - '" + ((Class) element).getName() + "' - IRI: " + getTagValue(element, "OWL2Entity","EntityIRI"));
							
							if (isSubClass((Class) element)) {
								// export as a subclass
								Collection<Class> superClass = ((Class) element).getSuperClass();
								for (Class supClass : superClass) {
									OwlApi.exportSubClass(getTagValue(element, Stereotypes.OWL_ENTITY, "EntityIRI"), getTagValue(supClass, Stereotypes.OWL_ENTITY, "EntityIRI")); 
								}
							} else {
								/**
								 *  export as a parent class - parent class is exported together with subclass so there is no need to export it separately
								 */
								// OwlApi.exportClass(getTagValue(element, "OWL2Entity","EntityIRI"));
							}
							
							exportClassAttributes((Class) element, "DataProperty");
						}
						else if (element instanceof Association)
						{
							
						}
					}
					
					OwlApi.saveOnto("functionalSyntax");
				}
			}
			if (packageExist == false) {
				JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Can not find package with stereotype '<<OWL2 ontology>>'");
				return;
			}
		} else {
			JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Open project first!");
		}		
	}
	
	/**
	 * 
	 * @param element
	 * @return Boolean
	 */
	private boolean isSubClass(Class element) {
		Collection<Classifier> general = element.getGeneral();
		if (general.isEmpty()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param element
	 * @param stereotypeName - attribute stereotype
	 */
	private void exportClassAttributes(Class element, String stereotypeName) {
		// range - type, domain - owner
		if (getStereotype((Element) element, stereotypeName) != null) {
			List<Property> attributes = element.getOwnedAttribute();
			for (int k = 0; k < attributes.size(); ++k)
			{
				Property attribute = attributes.get(k);
				if (StereotypesHelper.isElementStereotypedBy(attribute, stereotypeName)) {
					String attributeName = attribute.getName();
					String range = attribute.getType().getName();
					String domain = getTagValue(attribute.getOwner(), Stereotypes.OWL_ENTITY, "EntityIRI");
					OwlApi.exportDataProperty(attributeName, range, domain);
				}
			}
		}		
	}
	
	/**
	 * 
	 * @param e
	 * @param sterotypeName
	 * @param tagName
	 * @return String
	 */
	private String getTagValue(Element e, String stereotypeName, String tagName) {
		String result = "";
		Stereotype stereotype = getStereotype(e, stereotypeName);
		if (stereotype != null) {
			if (!StereotypesHelper.isElementStereotypedBy(e, stereotypeName)) {
				return "Stereotype named '" + stereotypeName + "' was not found in '" + e.getHumanName() + "'";
			}
			@SuppressWarnings("rawtypes")
			List value = StereotypesHelper.getStereotypePropertyValue(e, stereotype, tagName);
			for (int k = 0; k < value.size(); ++k)
			{
				result = (String) value.get(k); // a tag value
			}
		}
		
		return result;
	}
	
	/**
	 * Get stereotype from profile if it exist there, if not return null
	 * @param element
	 * @param stereotypeName
	 * @return Stereotype
	 */
	private Stereotype getStereotype(Element element, String stereotypeName) {
		Project project = Project.getProject(element); // find a profile
		Profile profile = StereotypesHelper.getProfile(project, Constants.PROFILE);
		try {
			return StereotypesHelper.getStereotype(project, stereotypeName, profile); // find a stereotype
		} catch(Exception e) {
			/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "stereotype: " + stereotypeName + " was not fount in '" + Constants.PROFILE + "'");
			return null;
		}
	}
}
