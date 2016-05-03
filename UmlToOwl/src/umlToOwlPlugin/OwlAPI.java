package umlToOwlPlugin;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
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
	 * Export class declaration
	 * 
	 * @param classIRI
	 */
	public void exportClass(String classIRI) {
		OWLClass owlClass = factory.getOWLClass(IRI.create(classIRI));
		
		OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(owlClass);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Export sub class of declaration
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
	 * Export data property (range & domain)
	 * 
	 * @param attributeName
	 * @param range
	 * @param domain
	 */
	public void exportDataProperty(String attributeIRI, String range, String domain) {
		OWLDataProperty dProperty = factory.getOWLDataProperty(IRI.create(attributeIRI));
		OWLDataPropertyDomainAxiom domainAxiom = factory.getOWLDataPropertyDomainAxiom(dProperty, factory.getOWLClass(IRI.create(domain)));
		OWLDataPropertyRangeAxiom rangeAxiom = factory.getOWLDataPropertyRangeAxiom(dProperty, factory.getOWLDatatype("xsd:" + range, new DefaultPrefixManager()));
		manager.addAxiom(ontology, rangeAxiom);
		manager.addAxiom(ontology, domainAxiom);
	}
	
	/**
	 * Export object property (range & domain)
	 * 
	 * @param propertyName
	 * @param range
	 * @param domain
	 */
	public void exportObjectProperty(String propertyIRI, String range, String domain) {
		OWLObjectProperty oProperty = factory.getOWLObjectProperty(IRI.create(propertyIRI));
		OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(oProperty, factory.getOWLClass(IRI.create(domain)));
		OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(oProperty, factory.getOWLClass(IRI.create(range)));
		manager.addAxiom(ontology, rangeAxiom);
		manager.addAxiom(ontology, domainAxiom);
	}
	
	/**
	 * Export sub data property of
	 * 
	 * @param subsettedProperty
	 * @param attributeName
	 */
	public void exportSubDataPropertyOf(String subsettedPropertyIRI, String attributeIRI) {
		OWLDataPropertyExpression subProperty = factory.getOWLDataProperty(IRI.create(attributeIRI));
		OWLDataPropertyExpression superProperty = factory.getOWLDataProperty(IRI.create(subsettedPropertyIRI));
		OWLSubDataPropertyOfAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Export sub object property of
	 * 
	 * @param subsettedProperty
	 * @param attributeName
	 */
	public void exportSubObjectPropertyOf(String subsettedPropertyIRI, String propertyIRI) {
		OWLObjectPropertyExpression subProperty = factory.getOWLObjectProperty(IRI.create(propertyIRI));
		OWLObjectPropertyExpression superProperty = factory.getOWLObjectProperty(IRI.create(subsettedPropertyIRI));
		OWLSubObjectPropertyOfAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Export disjoint union of
	 * @param classIRI
	 * @param classes
	 */
	public void exportDisJOintUnion(String classIRI, Collection<String> classes) {
		Set<OWLClassExpression> disjoinClassesSet = new LinkedHashSet<>();
		for (String classifier : classes) {
			disjoinClassesSet.add(factory.getOWLClass(IRI.create(classifier)));
		}
		OWLClass mainClass = factory.getOWLClass(IRI.create(classIRI));
		manager.addAxiom(ontology, factory.getOWLDisjointUnionAxiom(mainClass, disjoinClassesSet));
	}
	
	/**
	 * Export inverse object properties
	 * 
	 * @param forwardProperty
	 * @param inverseProperty
	 */
	public void exportInverseObjectProperties(String objIRI, String inverseIRI ) {
		OWLObjectProperty forward = factory.getOWLObjectProperty(IRI.create(objIRI));
		OWLObjectProperty inverse = factory.getOWLObjectProperty(IRI.create(inverseIRI));
		OWLInverseObjectPropertiesAxiom axiom = factory.getOWLInverseObjectPropertiesAxiom(forward, inverse);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Export named individuals
	 * 
	 * @param classIRI
	 * @param instanceName
	 */
	public void exportNamedIndividuals(String classIRI, String instanceName) {
		// @TODO - use instanceIRI instead of instanceName
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(IRI.create(ontoIRI + "#" + instanceName));
		OWLClass classExpressions = factory.getOWLClass(IRI.create(classIRI));
		OWLClassAssertionAxiom axiom = factory.getOWLClassAssertionAxiom(classExpressions, individual);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Export functional object properties
	 * 
	 * @param functional
	 * @param objectPropertyIRI
	 */
	public void exportFunctionalObjectProperty(String objectPropertyIRI) {
		OWLObjectProperty objProp = factory.getOWLObjectProperty(IRI.create(objectPropertyIRI));
		OWLFunctionalObjectPropertyAxiom axiom = factory.getOWLFunctionalObjectPropertyAxiom(objProp);
		manager.addAxiom(ontology, axiom);
	}
	
	/**
	 * Save ontology to file and show success message or error message
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
			JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Ontology successfully saved! Path: " + Constants.PATH_SAVE_TO);
		} catch (OWLOntologyStorageException e) {
			JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "error - " + e);
		}
	}
}
