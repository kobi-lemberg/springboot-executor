package com.kobi.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kobi.example.demo.dto.ReportDto;
import com.kobi.example.demo.entity.Report;
import com.kobi.example.demo.exception.ReportNotFoundException;
import com.kobi.example.demo.repository.ReportRepository;
import com.kobi.example.demo.service.ReportService;
import com.kobi.example.demo.service.client.ReportSchedulerClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@WebAppConfiguration
@SpringBootTest
@Slf4j
public class ApplicationIT {

	//todo add more tests especially negative scenarios

    private final ObjectMapper objectMapper = new ObjectMapper();

	private MockMvc mockMvc;

	@SpyBean
	private ReportService reportService;

	@SpyBean
	private ReportSchedulerClient reportSchedulerClient;

	@Autowired
	private ReportRepository repository;

	@Autowired
	WebApplicationContext wac;

	@Before
	public void init() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac)
				.build();
		repository.deleteAll();
	}

	@Test
	public void shouldReturnEmptyListWhenNoReportWasAssigned() throws Exception {
		assertEquals(0, repository.count());

		mockMvc.perform(get("/v1/reports"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().string(Collections.EMPTY_LIST.toString()));

		verify(reportService).getAllReports();
		verifyNoMoreInteractions(reportService);
	}

	@Test
	public void shouldReturnAllReports() throws Exception {
		assertEquals(0, repository.count());
		ReportDto firstReport = new ReportDto("report", 1, 1);
		//Insert report
		mockMvc.perform(post("/v1/report")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(firstReport)))
				.andExpect(status().isCreated())
				.andReturn();
		//Verify report was inserted
		assertEquals(1L, repository.count());

		mockMvc.perform(get("/v1/reports"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().string(objectMapper.writeValueAsString(repository.findAllByOrderByIdAsc())));

	}

	@Test
	public void shouldReturn404WhenReportNotFound() throws Exception {
		assertEquals(0, repository.count());

		MvcResult result = mockMvc.perform(get(String.format("/v1/report/%d", 1)))
				.andExpect(status().isNotFound())
				.andReturn();

		assertEquals(ReportNotFoundException.class, result.getResolvedException().getClass());
		verify(reportService).getReportById(1);
		verifyNoMoreInteractions(reportService);
	}

    @Test
    public void shouldCreateNewReportAndRunInExpectedOrder() throws Exception {
        assertEquals(0, repository.count());
		LocalTime time = LocalTime.now();
		//Creating first report
        ReportDto firstReport = new ReportDto("report", time.getHour(), time.getMinute() +1 );
        mockMvc.perform(post("/v1/report")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(firstReport)))
                .andExpect(status().isCreated())
                .andReturn();
		//Verify report was created
        verify(reportService).create(firstReport);
        //Sleep 2 minutes
		Thread.sleep(1000 * 60 * 2);

		//Verify first report was generated
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(reportSchedulerClient.getFileName()))) {
			List<String> lines = reader.lines().collect(Collectors.toList());
			assertEquals(1, lines.size());
			assertEquals( objectMapper.writeValueAsString(repository.findAllByOrderByIdAsc().get(0))
					, lines.get(0));
		}

		//Send 2 more report and verify order
		time = LocalTime.now();
		ReportDto secondReport = new ReportDto("report1", time.getHour(), time.getMinute() +1 );
		ReportDto thirdReport = new ReportDto("report2", time.getHour(), time.getMinute() +1 );
		mockMvc.perform(post("/v1/report")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(secondReport)))
				.andExpect(status().isCreated())
				.andReturn();
		verify(reportService).create(secondReport);
		mockMvc.perform(post("/v1/report")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(thirdReport)))
				.andExpect(status().isCreated())
				.andReturn();
		verify(reportService).create(thirdReport);

		//Wait 3 minutes to finish all reports
		Thread.sleep(1000 * 60 * 3);

		//Verify all reports are in the file at the right order
		List<Report> allReports = repository.findAllByOrderByIdAsc();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(reportSchedulerClient.getFileName()))) {
			List<String> lines = reader.lines().collect(Collectors.toList());
			assertEquals(3, lines.size());
			for (int i = 0 ; i < lines.size(); i++){
				assertEquals( objectMapper.writeValueAsString(allReports.get(i)), lines.get(i));
			}
		}

    }

	@Test
	public void shouldUpdateReport() throws Exception {
		LocalTime time = LocalTime.now();
		assertEquals(0, repository.count());
		ReportDto firstReport = new ReportDto("report", time.getHour(), time.getMinute() +1 );
		//Insert report
		mockMvc.perform(post("/v1/report")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(firstReport)))
				.andExpect(status().isCreated())
				.andReturn();
		//Verify report was inserted
		assertEquals(1L, repository.count());
		//Update report
		String newName = "new-name";
		Report created = repository.findAllByOrderByIdAsc().get(0);
		String reportStr = objectMapper.writeValueAsString(created); //Don't want to expose setter
		reportStr = reportStr.replace(created.getName(), newName);

		mockMvc.perform(put("/v1/report")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(reportStr))
				.andExpect(status().isOk())
				.andReturn();

		assertEquals(1L, repository.count());
		assertEquals(newName, repository.findAllByOrderByIdAsc().get(0).getName());
	}

	@Test
	public void shouldDeleteReport() throws Exception {
		assertEquals(0, repository.count());
		ReportDto firstReport = new ReportDto("report", 1, 1 );
		//Add new report
		mockMvc.perform(post("/v1/report")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(firstReport)))
				.andExpect(status().isCreated())
				.andReturn();

		//Verify report was inserted
		assertEquals(1L, repository.count());
		Report created = repository.findAllByOrderByIdAsc().get(0);

		mockMvc.perform(delete(String.format("/v1/report/%d",created.getId())))
				.andExpect(status().isOk())
				.andReturn();

		assertEquals(0L, repository.count());
	}

    @Test
    public void shouldReturnNotFoundWhenUserDeleteReportThatDoesNotExists() throws Exception {
        assertEquals(0, repository.count());

        mockMvc.perform(delete(String.format("/v1/report/%d",1)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

}
