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
	
	private OWLOntology myOntology;
	private OutputStream os;
	
	public UmlToOwlAction(String id, String name) {
		super(id, name, null, null);
	}
	
	public void actionPerformed(ActionEvent e) {
		Project p = Application.getInstance().getProjectsManager().getActiveProject(); // get active project
		if (p != null) {
			/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Model: " + p.getPrimaryModel().getName());
			Collection<Package> nestedPackage = p.getPrimaryModel().getNestedPackage(); // get collection of all packages in the project
			boolean packageExist = false; // by default package does not exist
			for (Iterator<Package> iterator = nestedPackage.iterator(); iterator.hasNext();) { // loop through all packages
				Package package1 = (Package) iterator.next();
				if (StereotypesHelper.isElementStereotypedBy(package1, "OWL2 ontology"))
				{	
					/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Package name: " + package1.getName());
					packageExist = true; // package found so set packageExist as true
					String ontoIRI = getTagValue(package1, "OWL2 ontology", "ontology IRI"); // get IRI of current package
					try {
						OwlApi = new OwlAPI(ontoIRI, "D:/ontologija.owl");
					} catch (OWLOntologyCreationException e1) {
						/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Error while creating OwlAPI object");
					}
					
					Collection<Element> ownedElement = package1.getOwnedElement(); // get collection of current package owned elements
					for (Iterator<Element> iterator2 = ownedElement.iterator(); iterator2.hasNext();) { // loop through all owned elements
						Element element = (Element) iterator2.next();
						if (element instanceof Class)
						{
							/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "This is class - '" + ((Class) element).getName() + "' - IRI: " + getTagValue(element, "OWL2Entity","EntityIRI"));
							OwlApi.exportClass(getTagValue(element, "OWL2Entity","EntityIRI"));
//							exportClassAttributes((Class) element);
								
						}
						else if (element instanceof Association)
						{
							
						}
					}
					
					OwlApi.saveOnto();
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
	
	private void exportClassAttributes(Class c1) {
		List<Property> attribute = c1.getAttribute();
		
	}
	
	private boolean isSubclass(Class c1) {
		Collection<Classifier> general = c1.getGeneral();
		if (general.isEmpty()) {
			return false;
		}
		
		return true;
	}
	
	private String getTagValue(Element e, String sterotypeName, String tagName) {
		String result = "";
		Project project = Project.getProject(e); // find a profile
		Profile profile = StereotypesHelper.getProfile(project, "OWL 2 profile");
		Stereotype stereotype = StereotypesHelper.getStereotype(project, sterotypeName, profile); // find a stereotype
		if (!StereotypesHelper.isElementStereotypedBy(e, sterotypeName)) {
			return "nera stereotipo";
		}
		@SuppressWarnings("rawtypes")
		List value = StereotypesHelper.getStereotypePropertyValue(e, stereotype, tagName);
		for (int k = 0; k < value.size(); ++k)
		{
			result = (String) value.get(k); // a tag value
		}
		return result;
	}
}
