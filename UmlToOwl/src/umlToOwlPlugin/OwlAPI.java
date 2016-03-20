package umlToOwlPlugin;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
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
	 * Set up required environment
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
	 * Add class declaration to ontology
	 * 
	 * @param classIRI
	 */
	public void exportClass(String classIRI) {
		/* Debug */ //JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Class IRI from exportClass: " + classIRI);		
		OWLClass owlClass = factory.getOWLClass(IRI.create(classIRI));
		
		OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(owlClass);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Add subclass declaration to ontology
	 * 
	 * @param childIRI
	 * @param parentIRI
	 */
	public void exportSubClass(String childIRI, String parentIRI) {
		OWLClass childClass = factory.getOWLClass(IRI.create(childIRI));
		OWLClass parentClass = factory.getOWLClass(IRI.create(parentIRI));
		
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(childClass, parentClass);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Add data property to ontology (range & domain)
	 * 
	 * @param attributeName
	 * @param range
	 * @param domain
	 */
	public void exportDataProperty(String attributeName, String range, String domain) {
		OWLDataProperty dProperty = factory.getOWLDataProperty(IRI.create(ontoIRI + "#" + attributeName));
		OWLDataPropertyDomainAxiom domainAxiom = factory.getOWLDataPropertyDomainAxiom(dProperty, factory.getOWLClass(IRI.create(domain)));
		OWLDataPropertyRangeAxiom rangeAxiom = factory.getOWLDataPropertyRangeAxiom(dProperty, factory.getOWLDatatype(IRI.create(range)));
		manager.addAxiom(ontology, rangeAxiom);
		manager.addAxiom(ontology, domainAxiom);
	}
	
	/**
	 * Save ontology to file
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
