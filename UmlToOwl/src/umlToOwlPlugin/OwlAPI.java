package umlToOwlPlugin;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;

import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;

public class OwlAPI {
	
	private OWLOntologyManager manager;
	private IRI ontoIRI;
	private IRI documentIRI;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	
	/**
	 * OwlAPI constructor
	 * 
	 * @param ontologyIRI
	 * @param saveTo
	 * @throws OWLOntologyCreationException
	 */
	public OwlAPI(String ontologyIRI, String saveTo) throws OWLOntologyCreationException {
		setUp(ontologyIRI, saveTo);
	}
	
	/**
	 * set up required environment
	 * 
	 * @param ontologyIRI
	 * @param saveTo
	 * @throws OWLOntologyCreationException
	 */
	private void setUp(String ontologyIRI, String saveTo) throws OWLOntologyCreationException {
		/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Set up in progress...");
		manager = OWLManager.createOWLOntologyManager(); // create OWL manager
		ontoIRI = IRI.create(ontologyIRI); // create ontology IRI
		documentIRI = IRI.create("file:///" + saveTo); // create document IRI (where ontology will be saved)
		ontology = manager.createOntology(ontoIRI); // create new ontology
		factory = manager.getOWLDataFactory(); // create data factory
	}
	
	/**
	 * add class IRI to ontology
	 * 
	 * @param classIRI
	 */
	public void exportClass(String classIRI) {
		/* Debug */ //JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Class IRI from exportClass: " + classIRI);		
		OWLClass owlClass = factory.getOWLClass(IRI.create(classIRI));
		
		OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(owlClass);
		manager.addAxiom(ontology, axiom);
	}
	
	public void exportSubClass(String childIRI, String parentIRI) {
		OWLClass childClass = factory.getOWLClass(IRI.create(childIRI));
		OWLClass parentClass = factory.getOWLClass(IRI.create(parentIRI));
		
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(childClass, parentClass);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * save ontology to file
	 */
	public void saveOnto(String fileFormat) {
		switch (fileFormat) {
			case "functionalSyntax":
				manager.setOntologyFormat(ontology, new FunctionalSyntaxDocumentFormat());
				break;
			case "manchesterSyntax":
				manager.setOntologyFormat(ontology, new ManchesterSyntaxDocumentFormat());
			case "owlXml":
				manager.setOntologyFormat(ontology, new OWLXMLDocumentFormat());
		}
		try {
			manager.saveOntology(ontology, documentIRI);
			/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Ontology successfully saved!");
		} catch (OWLOntologyStorageException e) {
			/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "error - " + e);
		}
	}
}
