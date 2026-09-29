package com.dizel.fikopar.service.report;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.service.SicilService;
import com.dizel.fikopar.service.criteria.CariCriteria;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.dto.SicilDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class CariReportService {

    private static final Locale TR = new Locale("tr", "TR");
    private final MessageSource messageSource;
    private final SicilService sicilService;

    public CariReportService(SicilService sicilService, MessageSource messageSource) {
        this.sicilService = sicilService;
        this.messageSource = messageSource;
    }

    private CellStyle baseStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle dateStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle(workbook));
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle currencyStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle(workbook));
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00;;"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle labelStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle(workbook));
        XSSFFont labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelFont.setFontHeight(14);
        style.setFont(labelFont);
        return style;
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(labelStyle(workbook));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public XSSFWorkbook createWorkbook() {
        return new XSSFWorkbook();
    }

    public XSSFSheet createSheet(XSSFWorkbook workbook, String sheetName) {
        XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setAutobreaks(false);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        sheet.setPrintGridlines(true);

        XSSFPrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);
        printSetup.setFitWidth((short) 1);
        printSetup.setFitHeight((short) 0);

        sheet.setRepeatingRows(CellRangeAddress.valueOf("1:4"));

        return sheet;
    }

    public int writeHeader(
        XSSFWorkbook workbook,
        XSSFSheet sheet,
        String hesapAdi,
        LocalDate baslangic,
        LocalDate bitis,
        DepoTipi depoTipi
    ) {
        int rowCount = 0;

        Row row = sheet.createRow(rowCount++);
        row.setHeight((short) 500);
        int columnCount = 0;
        createCell(workbook, sheet, row, columnCount++, "Hesap Adı:", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, hesapAdi, baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Başlangıç:", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount, baslangic, dateStyle(workbook));

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));

        row = sheet.createRow(rowCount++);
        row.setHeight((short) 500);
        columnCount = 0;
        createCell(workbook, sheet, row, columnCount++, "", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Bitiş:", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount, bitis, dateStyle(workbook));

        row = sheet.createRow(rowCount++);
        row.setHeight((short) 500);
        columnCount = 0;
        createCell(workbook, sheet, row, columnCount++, "", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", baseStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "", labelStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Depo:", labelStyle(workbook));
        createCell(
            workbook,
            sheet,
            row,
            columnCount,
            depoTipi == null ? "" : messageSource.getMessage("depoTipi." + depoTipi.name(), null, TR),
            baseStyle(workbook)
        );

        row = sheet.createRow(rowCount++);
        row.setHeight((short) 500);

        columnCount = 0;
        createCell(workbook, sheet, row, columnCount++, "Tarih", headerStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Evrak No", headerStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Vade", headerStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Tipi", headerStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Depo Tipi", headerStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Açıklama", headerStyle(workbook));
        createCell(workbook, sheet, row, columnCount++, "Borç", headerStyle(workbook));
        createCell(workbook, sheet, row, columnCount, "Alacak", headerStyle(workbook));

        return rowCount;
    }

    private void createCell(XSSFWorkbook workbook, XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        CellStyle applyStyle = style;
        if (valueOfCell == null) {
            cell.setCellValue("");
            cell.setCellStyle(applyStyle);
            return;
        }

        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            applyStyle = dateStyle(workbook);
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof BigDecimal) {
            applyStyle = currencyStyle(workbook);
            cell.setCellValue(((BigDecimal) valueOfCell).doubleValue());
        } else {
            cell.setCellValue((Boolean) valueOfCell);
        }
        cell.setCellStyle(applyStyle);
    }

    private void writeSumRow(
        XSSFWorkbook workbook,
        XSSFSheet sheet,
        CellStyle style,
        int rowCount,
        String label,
        BigDecimal borc,
        BigDecimal alacak
    ) {
        // labels
        CellStyle labelStyle = workbook.createCellStyle();
        labelStyle.cloneStyleFrom(style);
        labelStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelFont.setFontHeight(14);
        labelStyle.setFont(labelFont);
        // totals of borc and alacak
        Row row = sheet.createRow(rowCount++);
        row.setHeight((short) 500);
        int columnCount = 0;
        createCell(workbook, sheet, row, columnCount++, "", style);
        createCell(workbook, sheet, row, columnCount++, "", style);
        createCell(workbook, sheet, row, columnCount++, "", style);
        createCell(workbook, sheet, row, columnCount++, "", style);
        createCell(workbook, sheet, row, columnCount++, "", style);
        createCell(workbook, sheet, row, columnCount++, label, labelStyle);
        createCell(workbook, sheet, row, columnCount++, borc, style);
        createCell(workbook, sheet, row, columnCount, alacak, style);
    }

    public XSSFWorkbook write(List<CariDTO> list, CariCriteria criteria) {
        XSSFWorkbook workbook = createWorkbook();
        XSSFSheet sheet = createSheet(workbook, "Cari");

        int rowCount = writeHeader(
            workbook,
            sheet,
            criteria.getSicilId() == null
                ? ""
                : sicilService.findOne(criteria.getSicilId().getEquals()).map(SicilDTO::getHesapadi).orElse(""),
            criteria.getTarih() == null ? null : criteria.getTarih().getGreaterThanOrEqual(),
            criteria.getTarih() == null ? null : criteria.getTarih().getLessThanOrEqual(),
            criteria.getDepotipi() == null ? null : criteria.getDepotipi().getEquals()
        );

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        int columnCount = 0;
        for (CariDTO cari : list) {
            Row row = sheet.createRow(rowCount++);
            row.setHeight((short) 500);
            columnCount = 0;
            createCell(workbook, sheet, row, columnCount++, cari.getTarih(), style);
            createCell(workbook, sheet, row, columnCount++, cari.getEvrakno(), style);
            createCell(workbook, sheet, row, columnCount++, cari.getVade(), style);
            createCell(
                workbook,
                sheet,
                row,
                columnCount++,
                messageSource.getMessage("evrakTipi." + cari.getTipi().name(), null, TR),
                style
            );
            createCell(
                workbook,
                sheet,
                row,
                columnCount++,
                messageSource.getMessage("depoTipi." + cari.getDepotipi().name(), null, TR),
                style
            );
            createCell(workbook, sheet, row, columnCount++, cari.getAciklama(), style);
            createCell(workbook, sheet, row, columnCount++, cari.getBorc(), style);
            createCell(workbook, sheet, row, columnCount, cari.getAlacak(), style);
        }

        BigDecimal borcToplam = list.stream().map(CariDTO::getBorc).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal alacakToplam = list.stream().map(CariDTO::getAlacak).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal bakiye = borcToplam.subtract(alacakToplam);

        writeSumRow(workbook, sheet, style, rowCount++, "Toplam", borcToplam, alacakToplam);
        writeSumRow(
            workbook,
            sheet,
            style,
            rowCount++,
            "Baki̇ye",
            bakiye.compareTo(BigDecimal.ZERO) > 0 ? bakiye : BigDecimal.ZERO,
            bakiye.compareTo(BigDecimal.ZERO) < 0 ? bakiye.multiply(BigDecimal.valueOf(-1)) : BigDecimal.ZERO
        );

        BigDecimal depo1BorcToplam = list
            .stream()
            .filter(cari -> cari.getDepotipi() == DepoTipi.DEPO1)
            .map(CariDTO::getBorc)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depo1AlacakToplam = list
            .stream()
            .filter(cari -> cari.getDepotipi() == DepoTipi.DEPO1)
            .map(CariDTO::getAlacak)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depo1Bakiye = depo1BorcToplam.subtract(depo1AlacakToplam);

        writeSumRow(workbook, sheet, style, rowCount++, "1.Depo Toplam", depo1BorcToplam, depo1AlacakToplam);
        writeSumRow(
            workbook,
            sheet,
            style,
            rowCount++,
            "1.Depo Baki̇ye",
            depo1Bakiye.compareTo(BigDecimal.ZERO) > 0 ? depo1Bakiye : BigDecimal.ZERO,
            depo1Bakiye.compareTo(BigDecimal.ZERO) < 0 ? depo1Bakiye.multiply(BigDecimal.valueOf(-1)) : BigDecimal.ZERO
        );

        BigDecimal depo2BorcToplam = list
            .stream()
            .filter(cari -> cari.getDepotipi() == DepoTipi.DEPO2)
            .map(CariDTO::getBorc)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depo2AlacakToplam = list
            .stream()
            .filter(cari -> cari.getDepotipi() == DepoTipi.DEPO2)
            .map(CariDTO::getAlacak)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depo2Bakiye = depo2BorcToplam.subtract(depo2AlacakToplam);

        writeSumRow(workbook, sheet, style, rowCount++, "2.Depo Toplam", depo2BorcToplam, depo2AlacakToplam);
        writeSumRow(
            workbook,
            sheet,
            style,
            rowCount,
            "2.Depo Baki̇ye",
            depo2Bakiye.compareTo(BigDecimal.ZERO) > 0 ? depo2Bakiye : BigDecimal.ZERO,
            depo2Bakiye.compareTo(BigDecimal.ZERO) < 0 ? depo2Bakiye.multiply(BigDecimal.valueOf(-1)) : BigDecimal.ZERO
        );

        for (int i = 0; i <= columnCount; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}
