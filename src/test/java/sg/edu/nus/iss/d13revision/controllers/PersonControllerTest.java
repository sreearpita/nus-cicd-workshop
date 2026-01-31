package sg.edu.nus.iss.d13revision.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import sg.edu.nus.iss.d13revision.models.Person;
import sg.edu.nus.iss.d13revision.services.PersonService;

@SpringBootTest
@AutoConfigureMockMvc
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    private List<Person> personList;

    @BeforeEach
    public void setUp() {
        personList = new ArrayList<>();
        personList.add(new Person("12345", "Mark", "Zuckerberg"));
        personList.add(new Person("67890", "Elon", "Musk"));
    }

    // ======================== Index/Home Tests ========================

    @Test
    public void testIndexPageGet() throws Exception {
        mockMvc.perform(get("/person/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    public void testHomePageGet() throws Exception {
        mockMvc.perform(get("/person/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void testIndexPageGet2() throws Exception {
        mockMvc.perform(get("/person/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void testIndexPageMessageAttribute() throws Exception {
        mockMvc.perform(get("/person/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", notNullValue()));
    }

    // ======================== Test Retrieve Tests ========================

    @Test
    public void testGetAllPersonsEndpoint() throws Exception {
        when(personService.getPersons()).thenReturn(personList);

        mockMvc.perform(get("/person/testRetrieve"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Mark")))
                .andExpect(jsonPath("$[0].lastName", is("Zuckerberg")))
                .andExpect(jsonPath("$[1].firstName", is("Elon")))
                .andExpect(jsonPath("$[1].lastName", is("Musk")));

        verify(personService, times(1)).getPersons();
    }

    @Test
    public void testGetAllPersonsReturnsList() throws Exception {
        when(personService.getPersons()).thenReturn(personList);

        mockMvc.perform(get("/person/testRetrieve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void testGetAllPersonsEmptyList() throws Exception {
        when(personService.getPersons()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/person/testRetrieve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetAllPersonsContentType() throws Exception {
        when(personService.getPersons()).thenReturn(personList);

        mockMvc.perform(get("/person/testRetrieve"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    // ======================== Person List Tests ========================

    @Test
    public void testPersonListPage() throws Exception {
        when(personService.getPersons()).thenReturn(personList);

        mockMvc.perform(get("/person/personList"))
                .andExpect(status().isOk())
                .andExpect(view().name("personList"))
                .andExpect(model().attributeExists("persons"))
                .andExpect(model().attribute("persons", hasSize(2)));

        verify(personService, times(1)).getPersons();
    }

    @Test
    public void testPersonListPagePersonsAttribute() throws Exception {
        when(personService.getPersons()).thenReturn(personList);

        mockMvc.perform(get("/person/personList"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("persons", notNullValue()));
    }

    @Test
    public void testPersonListPageEmptyList() throws Exception {
        when(personService.getPersons()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/person/personList"))
                .andExpect(status().isOk())
                .andExpect(view().name("personList"))
                .andExpect(model().attribute("persons", hasSize(0)));
    }

    @Test
    public void testPersonListPageMultiplePersons() throws Exception {
        List<Person> multiplePersons = new ArrayList<>();
        multiplePersons.add(new Person("1", "John", "Doe"));
        multiplePersons.add(new Person("2", "Jane", "Smith"));
        multiplePersons.add(new Person("3", "Bob", "Johnson"));

        when(personService.getPersons()).thenReturn(multiplePersons);

        mockMvc.perform(get("/person/personList"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("persons", hasSize(3)));
    }

    // ======================== Add Person (GET) Tests ========================

    @Test
    public void testShowAddPersonPageGet() throws Exception {
        mockMvc.perform(get("/person/addPerson"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attributeExists("personForm"));
    }

    @Test
    public void testShowAddPersonPageModelAttribute() throws Exception {
        mockMvc.perform(get("/person/addPerson"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("personForm", notNullValue()));
    }

    // ======================== Add Person (POST) Tests ========================

    @Test
    public void testSavePersonWithValidData() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "Steve")
                .param("lastName", "Jobs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/person/personList"));

        verify(personService, times(1)).addPerson(any());
    }

    @Test
    public void testSavePersonWithValidNames() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "Bill")
                .param("lastName", "Gates"))
                .andExpect(status().is3xxRedirection());

        verify(personService, times(1)).addPerson(any());
    }

    @Test
    public void testSavePersonWithEmptyFirstName() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "")
                .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(personService, times(0)).addPerson(any());
    }

    @Test
    public void testSavePersonWithEmptyLastName() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "John")
                .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(personService, times(0)).addPerson(any());
    }

    @Test
    public void testSavePersonWithBothNamesEmpty() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "")
                .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(personService, times(0)).addPerson(any());
    }

    @Test
    public void testSavePersonWithNullFirstName() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"));

        verify(personService, times(0)).addPerson(any());
    }

    @Test
    public void testSavePersonWithNullLastName() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "John"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"));

        verify(personService, times(0)).addPerson(any());
    }

    @Test
    public void testSavePersonRedirectsToPersonList() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "Alice")
                .param("lastName", "Wonder"))
                .andExpect(redirectedUrl("/person/personList"));
    }

    @Test
    public void testSavePersonWithSpecialCharacters() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "Jean-Claude")
                .param("lastName", "Van Damme"))
                .andExpect(status().is3xxRedirection());

        verify(personService, times(1)).addPerson(any());
    }

    // ======================== Person To Edit Tests ========================

    @Test
    public void testPersonToEdit() throws Exception {
        Person personToEdit = personList.get(0);

        mockMvc.perform(post("/person/personToEdit")
                .param("id", personToEdit.getId())
                .param("firstName", personToEdit.getFirstName())
                .param("lastName", personToEdit.getLastName()))
                .andExpect(status().isOk())
                .andExpect(view().name("editPerson"))
                .andExpect(model().attributeExists("per"));
    }

    @Test
    public void testPersonToEditWithValidPerson() throws Exception {
        mockMvc.perform(post("/person/personToEdit")
                .param("id", "12345")
                .param("firstName", "Mark")
                .param("lastName", "Zuckerberg"))
                .andExpect(status().isOk())
                .andExpect(view().name("editPerson"));
    }

    @Test
    public void testPersonToEditModelAttribute() throws Exception {
        mockMvc.perform(post("/person/personToEdit")
                .param("id", "12345")
                .param("firstName", "Mark")
                .param("lastName", "Zuckerberg"))
                .andExpect(model().attribute("per", notNullValue()));
    }

    // ======================== Person Edit (Update) Tests ========================

    @Test
    public void testPersonEdit() throws Exception {
        mockMvc.perform(post("/person/personEdit")
                .param("id", "12345")
                .param("firstName", "Mark")
                .param("lastName", "Zuckerberg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/person/personList"));

        verify(personService, times(1)).updatePerson(any());
    }

    @Test
    public void testPersonEditUpdatesAndRedirects() throws Exception {
        mockMvc.perform(post("/person/personEdit")
                .param("id", "67890")
                .param("firstName", "Elon")
                .param("lastName", "Musk"))
                .andExpect(status().is3xxRedirection());

        verify(personService, times(1)).updatePerson(any());
    }

    @Test
    public void testPersonEditWithDifferentValues() throws Exception {
        mockMvc.perform(post("/person/personEdit")
                .param("id", "12345")
                .param("firstName", "NewName")
                .param("lastName", "NewLastName"))
                .andExpect(status().is3xxRedirection());

        verify(personService, times(1)).updatePerson(any());
    }

    // ======================== Person Delete Tests ========================

    @Test
    public void testPersonDelete() throws Exception {
        mockMvc.perform(post("/person/personDelete")
                .param("id", "12345")
                .param("firstName", "Mark")
                .param("lastName", "Zuckerberg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/person/personList"));

        verify(personService, times(1)).removePerson(any());
    }

    @Test
    public void testPersonDeleteRemovesAndRedirects() throws Exception {
        mockMvc.perform(post("/person/personDelete")
                .param("id", "67890")
                .param("firstName", "Elon")
                .param("lastName", "Musk"))
                .andExpect(status().is3xxRedirection());

        verify(personService, times(1)).removePerson(any());
    }

    @Test
    public void testPersonDeleteWithMultiplePeople() throws Exception {
        mockMvc.perform(post("/person/personDelete")
                .param("id", "12345")
                .param("firstName", "Mark")
                .param("lastName", "Zuckerberg"))
                .andExpect(redirectedUrl("/person/personList"));

        verify(personService, times(1)).removePerson(any());
    }

    // ======================== Integration/Flow Tests ========================

    @Test
    public void testAddPersonFlow() throws Exception {
        // Add person
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "NewPerson")
                .param("lastName", "Name"))
                .andExpect(status().is3xxRedirection());

        verify(personService, times(1)).addPerson(any());
    }

    @Test
    public void testFullPersonWorkflow() throws Exception {
        // Get list
        when(personService.getPersons()).thenReturn(personList);
        mockMvc.perform(get("/person/personList"))
                .andExpect(status().isOk());

        // Add person
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "Test")
                .param("lastName", "User"))
                .andExpect(status().is3xxRedirection());

        // Update person
        mockMvc.perform(post("/person/personEdit")
                .param("id", "12345")
                .param("firstName", "Updated")
                .param("lastName", "Name"))
                .andExpect(status().is3xxRedirection());

        // Delete person
        mockMvc.perform(post("/person/personDelete")
                .param("id", "12345")
                .param("firstName", "Updated")
                .param("lastName", "Name"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testMultipleAddPersonRequests() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "Person1")
                .param("lastName", "Lastname1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "Person2")
                .param("lastName", "Lastname2"))
                .andExpect(status().is3xxRedirection());

        verify(personService, times(2)).addPerson(any());
    }

}
