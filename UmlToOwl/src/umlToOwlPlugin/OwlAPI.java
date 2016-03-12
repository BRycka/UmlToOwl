package umlToOwlPlugin;


import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;

import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;

public class OwlAPI {
	
	private OWLOntologyManager manager;
	private IRI ontoIRI;
	private IRI documentIRI;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	
	public OwlAPI(String ontologyIRI, String saveTo) throws OWLOntologyCreationException {
		setUp(ontologyIRI, saveTo);
	}
	
	private void setUp(String ontologyIRI, String saveTo) throws OWLOntologyCreationException {
		/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Set up in progress...");
		manager = OWLManager.createOWLOntologyManager(); // create OWL manager
		ontoIRI = IRI.create(ontologyIRI); // create ontology IRI
		documentIRI = IRI.create("file:///" + saveTo); // create document IRI (where ontology will be saved)
		ontology = manager.createOntology(ontoIRI); // create new ontology
		factory = manager.getOWLDataFactory(); // create data factory		
	}
	
	public void exportClass(String classIRI) {
		/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Class IRI from exportClass: " + classIRI);
		OWLClass owlClass = factory.getOWLClass(IRI.create(classIRI));
		OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(owlClass);
		
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		manager.applyChange(addAxiom);
	}
	
	public void saveOnto() {
		try {
			manager.setOntologyFormat(ontology, new FunctionalSyntaxDocumentFormat());
			manager.saveOntology(ontology, documentIRI);
			/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Ontology successfully saved!");
		} catch (OWLOntologyStorageException e) {
			/* Debug */ JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "error - " + e);
		}
	}
}
