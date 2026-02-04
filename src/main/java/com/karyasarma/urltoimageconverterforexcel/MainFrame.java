package com.karyasarma.urltoimageconverterforexcel;

import com.karyasarma.urltoimageconverterforexcel.processor.ExcelUrlToImageConverter;
import com.karyasarma.urltoimageconverterforexcel.processor.ExcelUrlToImageConverterConfigs;
import com.karyasarma.urltoimageconverterforexcel.processor.ExcelUrlToImageConverterListener;
import com.karyasarma.urltoimageconverterforexcel.ui.model.SheetCbModel;
import com.karyasarma.urltoimageconverterforexcel.util.AppUtils;
import com.karyasarma.urltoimageconverterforexcel.util.FileTypeFilter;
import com.karyasarma.urltoimageconverterforexcel.util.FileUtils;
import dnd.FileDrop;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author Daniel Joi Partogi Hutapea
 */
@SuppressWarnings("FieldCanBeLocal")
public class MainFrame extends JFrame implements ExcelUrlToImageConverterListener
{
    private static final DateTimeFormatter OUTPUT_FILE_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter INFO_TA_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FileFilter xlsxFileFilter;
    private JFileChooser fileChooser;

    private JMenu fileMenu;
    private JMenuItem openMenuItem;
    private JMenuItem exitMenuItem;

    private JMenu toolsMenu;
    private JMenuItem clearMenuItem;

    private JPanel settingsPanel;
    private ButtonGroup operationTypeBg;
    private JRadioButton bulkRegisterMerchantRb;
    private JRadioButton bulkUpdateMerchantRb;
    private JRadioButton bulkUpgradeMerchantRb;

    private JSplitPane mainContainerSp;
    private JLabel dropFileContainer;
    private JScrollPane infoSp;
    private JTextArea infoTa;

    private JPanel statusBar;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    private JLabel fileInputLbl;
    private JTextField fileInputTf;

    private JLabel fileOutputLbl;
    private JTextField fileOutputTf;

    private JLabel sheetNameLbl;
    private JComboBox<SheetCbModel> sheetNameCb;

    private JSpinner fromRowSpinner;
    private JLabel fromRowLbl;

    private JLabel toRowLbl;
    private JSpinner toRowSpinner;

    private JLabel imageRowHeightLbl;
    private JSpinner imageRowHeightSpinner;

    private JLabel imageColumnWidthLbl;
    private JSpinner imageColumnWidthSpinner;
    private JLabel imageColumnWidthHintLbl;

    private JButton processBtn;

    private volatile boolean isUploadOnProgress = false;

    public MainFrame()
    {
        initComponents();
    }

    private void initComponents()
    {
        // Init JFileChooser
        xlsxFileFilter = new FileTypeFilter(".xlsx", "Excel Workbook (*.xlsx)");
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(xlsxFileFilter);
        fileChooser.setFileFilter(xlsxFileFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        setJMenuBar(new JMenuBar());
        var jMenuBar = getJMenuBar();

        // File Menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        jMenuBar.add(fileMenu);

        openMenuItem = new JMenuItem("Open");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openMenuItem.addActionListener(_ ->
        {
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                loadFile(fileChooser.getSelectedFile());
            }
        });
        fileMenu.add(openMenuItem);

        fileMenu.addSeparator();

        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        exitMenuItem.addActionListener(_ -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        fileMenu.add(exitMenuItem);

        // Tools Menu
        toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        jMenuBar.add(toolsMenu);

        clearMenuItem = new JMenuItem("Clear");
        clearMenuItem.setMnemonic(KeyEvent.VK_C);
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        clearMenuItem.addActionListener(_ -> clear());
        toolsMenu.add(clearMenuItem);

        // Settings Container
        var settingsPanelBorder = BorderFactory.createTitledBorder("Settings:");
        settingsPanelBorder.setTitleJustification(TitledBorder.CENTER);

        settingsPanel = new JPanel();
        settingsPanel.setBorder(settingsPanelBorder);
        getContentPane().add(settingsPanel, BorderLayout.NORTH);

        fileInputLbl = new JLabel("File Input:");

        fileInputTf = new JTextField();
        fileInputTf.setEditable(false);

        fileOutputLbl = new JLabel("File Output:");

        fileOutputTf = new JTextField();
        fileOutputTf.setEditable(false);

        sheetNameLbl = new JLabel("Sheet Name:");

        sheetNameCb = new JComboBox<>();
        sheetNameCb.addItemListener(evt ->
        {
            if(evt.getStateChange() == ItemEvent.SELECTED)
            {
                var item = (SheetCbModel) evt.getItem();
                updateRowRangeSpinner(item);
            }
        });

        fromRowLbl = new JLabel("Row Range From:");

        fromRowSpinner = new JSpinner();
        fromRowSpinner.setPreferredSize(new Dimension(100, 0));
        fromRowSpinner.setModel(new SpinnerNumberModel(1, 1, 1, 1));

        toRowLbl = new JLabel("Until:");
        toRowLbl.setHorizontalAlignment(SwingConstants.CENTER);

        toRowSpinner = new JSpinner();
        toRowSpinner.setPreferredSize(new Dimension(100, 0));
        toRowSpinner.setModel(new SpinnerNumberModel(1, 1, 1, 1));

        imageRowHeightLbl = new JLabel("Image Row Height:");

        imageRowHeightSpinner = new JSpinner(new SpinnerNumberModel(300, 1, 1000, 1));
        imageRowHeightSpinner.setPreferredSize(new Dimension(100, 0));

        imageColumnWidthLbl = new JLabel("Image Column Width:");

        imageColumnWidthSpinner = new JSpinner(new SpinnerNumberModel(40, 1, 1000, 1));
        imageColumnWidthSpinner.setPreferredSize(new Dimension(100, 0));

        imageColumnWidthHintLbl = new JLabel("(The width is in units of 1/256th of a character width.)");

        processBtn = new JButton("Process");
        processBtn.setPreferredSize(new Dimension(0, 200));
        processBtn.addActionListener(this::onProcessBtnClicked);
        processBtn.setMnemonic(KeyEvent.VK_P);
        processBtn.requestFocus();

        var settingsPanelLayout = new GroupLayout(settingsPanel);
        settingsPanelLayout.setAutoCreateGaps(true);
        settingsPanelLayout.setAutoCreateContainerGaps(true);

        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    settingsPanelLayout
                        .createSequentialGroup()
                        .addGroup(
                            settingsPanelLayout
                                .createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(fileInputLbl)
                                .addComponent(fileOutputLbl)
                                .addComponent(sheetNameLbl)
                                .addComponent(fromRowLbl)
                                .addComponent(imageRowHeightLbl)
                        )
                        .addGroup(
                            settingsPanelLayout
                                .createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(fileInputTf)
                                .addComponent(fileOutputTf)
                                .addComponent(sheetNameCb)
                                .addGroup(
                                    settingsPanelLayout.createSequentialGroup()
                                        .addComponent(fromRowSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(toRowLbl)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(toRowSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                )
                                .addGroup(
                                    settingsPanelLayout.createSequentialGroup()
                                        .addComponent(imageRowHeightSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(imageColumnWidthLbl)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(imageColumnWidthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(imageColumnWidthHintLbl)
                                )
                        )
                )
                .addComponent(processBtn, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
        );

        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createSequentialGroup()
                .addGroup(
                    settingsPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fileInputLbl)
                        .addComponent(fileInputTf)
                )
                .addGroup(
                    settingsPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fileOutputLbl)
                        .addComponent(fileOutputTf)
                )
                .addGroup(
                    settingsPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(sheetNameLbl)
                        .addComponent(sheetNameCb)
                )
                .addGroup(
                    settingsPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fromRowLbl)
                        .addComponent(fromRowSpinner)
                        .addComponent(toRowLbl)
                        .addComponent(toRowSpinner)
                )
                .addGroup(
                    settingsPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(imageRowHeightLbl)
                        .addComponent(imageRowHeightSpinner)
                        .addComponent(imageColumnWidthLbl)
                        .addComponent(imageColumnWidthSpinner)
                        .addComponent(imageColumnWidthHintLbl)
                )
                .addGroup(
                    settingsPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(processBtn, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                )
        );

        settingsPanel.setLayout(settingsPanelLayout);

        bulkRegisterMerchantRb = new JRadioButton("Bulk Register Merchant");
        bulkRegisterMerchantRb.setMnemonic(KeyEvent.VK_R);
        bulkRegisterMerchantRb.setActionCommand("REGISTER");
        bulkRegisterMerchantRb.setSelected(true);
        settingsPanel.add(bulkRegisterMerchantRb);

        bulkUpdateMerchantRb = new JRadioButton("Bulk Update Merchant");
        bulkUpdateMerchantRb.setMnemonic(KeyEvent.VK_U);
        bulkUpdateMerchantRb.setActionCommand("UPDATE");
        settingsPanel.add(bulkUpdateMerchantRb);

        bulkUpgradeMerchantRb = new JRadioButton("Bulk Upgrade Merchant");
        bulkUpgradeMerchantRb.setMnemonic(KeyEvent.VK_G);
        bulkUpgradeMerchantRb.setActionCommand("UPGRADE");
        settingsPanel.add(bulkUpgradeMerchantRb);

        operationTypeBg = new ButtonGroup();
        operationTypeBg.add(bulkRegisterMerchantRb);
        operationTypeBg.add(bulkUpdateMerchantRb);
        operationTypeBg.add(bulkUpgradeMerchantRb);

        // Main Container
        mainContainerSp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainContainerSp.setDividerLocation(150);
        getContentPane().add(mainContainerSp, BorderLayout.CENTER);

        dropFileContainer = new JLabel("Drag your file here.");
        dropFileContainer.setBorder(BorderFactory.createTitledBorder(""));
        dropFileContainer.setHorizontalAlignment(SwingConstants.CENTER);
        new FileDrop(dropFileContainer, this::onFileDropped);
        mainContainerSp.setLeftComponent(dropFileContainer);

        infoTa = new JTextArea();
        infoTa.setEditable(false);

        infoSp = new JScrollPane(infoTa);
        TitledBorder infoSpBorder = BorderFactory.createTitledBorder("Info:");
        infoSpBorder.setTitleJustification(TitledBorder.CENTER);
        infoSp.setBorder(infoSpBorder);
        mainContainerSp.setRightComponent(infoSp);

        statusBar = new JPanel();
        statusBar.setPreferredSize(new Dimension(0, 25));
        statusBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statusBar.setBorder(new EmptyBorder(5, 0, 5, 0));
        statusBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setFont(new Font(progressBar.getFont().getName(), Font.PLAIN, 10));
        progressBar.setMaximumSize(new Dimension(200, 100));
        progressBar.setStringPainted(true);
        statusBar.add(progressBar);

        statusLabel = new JLabel("Status: Idle");
        statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.PLAIN, 11));
        statusBar.add(statusLabel);

        var iconUrl = MainFrame.class.getClassLoader().getResource("images/icon.png");

        if(iconUrl != null)
        {
            var imageIcon = new ImageIcon(iconUrl);
            setIconImage(imageIcon.getImage());
        }

        pack();
        setSize(900, 700);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                var additionalMessage = isUploadOnProgress ? "Uploading is still on progress. " : "";

                int confirmed = JOptionPane.showConfirmDialog
                    (
                        MainFrame.this,
                        additionalMessage + "Are you sure you want to exit the program?",
                        "Exit Dialog",
                        JOptionPane.YES_NO_OPTION
                    );

                if(confirmed == JOptionPane.YES_OPTION)
                {
                    System.exit(0);
                }
            }
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("URL to Image Converter for Microsoft Excel (by Daniel J. P. Hutapea)");
    }

    private synchronized void onFileDropped(File[] arrayOfDroppedFile)
    {
        if(isUploadOnProgress)
        {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Upload is still on progress. Please wait!"));
            return;
        }

        var filteredFiles = new ArrayList<File>();

        for(var file : arrayOfDroppedFile)
        {
            if(!file.isDirectory() && xlsxFileFilter.accept(file))
            {
                filteredFiles.add(file);
            }
        }

        if(filteredFiles.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Error: No Excel file found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            String multipleDroppedFilesWarning = null;

            if(filteredFiles.size() > 1)
            {
                multipleDroppedFilesWarning = """
                    WARNING: Only a single file can be processed at a time.
                    "Only file '%s' will be processed."
                    """.formatted(filteredFiles.getFirst().getAbsolutePath()).trim();
            }

            loadFile(filteredFiles.getFirst(), multipleDroppedFilesWarning);
        }
    }

    private synchronized void loadFile(File file)
    {
        loadFile(file, null);
    }

    private synchronized void loadFile(File file, String warningMessage)
    {
        try
        (
            var fis = new FileInputStream(file);
            var workbook = new XSSFWorkbook(fis)
        )
        {
            clear();

            if(warningMessage != null)
            {
                onInfo(warningMessage);
            }

            fileInputTf.setText(file.getAbsolutePath());

            var outputFile = new File(file.getParent(), FileUtils.getFileNameWithoutExtension(file) + "_RESULT_" + OUTPUT_FILE_DTF.format(ZonedDateTime.now()) + ".xlsx");
            fileOutputTf.setText(outputFile.getAbsolutePath());

            var arrayOfSheetCbModel = new SheetCbModel[workbook.getNumberOfSheets()];
            var index = 0;

            for(var sheet : workbook)
            {
                var sheetCbModel = new SheetCbModel();
                sheetCbModel.setFile(file);
                sheetCbModel.setSheetName(sheet.getSheetName());
                sheetCbModel.setSheetIndex(workbook.getSheetIndex(sheet));
                sheetCbModel.setRowCount(sheet.getLastRowNum() + 1);
                arrayOfSheetCbModel[index] = sheetCbModel;

                if(index == 0)
                {
                    updateRowRangeSpinner(sheetCbModel);
                }

                index++;
            }

            sheetNameCb.setModel(new DefaultComboBoxModel<>(arrayOfSheetCbModel));
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onProcessBtnClicked(ActionEvent evt)
    {
        var thread = new Thread(() ->
        {
            var item = (SheetCbModel) sheetNameCb.getSelectedItem();

            if(item == null)
            {
                JOptionPane.showMessageDialog(this, "Error: Please select a sheet first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            var configs = new ExcelUrlToImageConverterConfigs();
            configs.setFileInput(item.getFile());
            configs.setFileOutput(new File(fileOutputTf.getText()));
            configs.setSheetIndex(item.getSheetIndex());
            configs.setRowIndexStart(Integer.parseInt(fromRowSpinner.getValue().toString()));
            configs.setRowIndexEnd(Integer.parseInt(toRowSpinner.getValue().toString()));
            configs.setRowHeight(Integer.parseInt(imageRowHeightSpinner.getValue().toString()));
            configs.setColumnWidth(Integer.parseInt(imageColumnWidthSpinner.getValue().toString()));

            new ExcelUrlToImageConverter(configs, this).process();
        });
        thread.setDaemon(true);
        thread.start();
    }

    private synchronized void inProgress(boolean isUploadOnProgress)
    {
        this.isUploadOnProgress = isUploadOnProgress;

        openMenuItem.setEnabled(!isUploadOnProgress);
        clearMenuItem.setEnabled(!isUploadOnProgress);
        dropFileContainer.setEnabled(!isUploadOnProgress);
    }

    private synchronized void clear()
    {
        fileInputTf.setText("");
        fileOutputTf.setText("");
        sheetNameCb.setModel(new DefaultComboBoxModel<>());
        fromRowSpinner.setModel(new SpinnerNumberModel(1, 1, 1, 1));
        toRowSpinner.setModel(new SpinnerNumberModel(1, 1, 1, 1));

        statusLabel.setText("Status: Idle");
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        infoTa.setText("");
    }

    private void updateRowRangeSpinner(SheetCbModel item)
    {
        fromRowSpinner.setModel(new SpinnerNumberModel(1, 1, item.getRowCount(), 1));
        toRowSpinner.setModel(new SpinnerNumberModel(item.getRowCount(), 1, item.getRowCount(), 1));
    }

    @Override
    public void onStart()
    {
        infoTa.setText("");
        appendInfo("Process starting...");
        inProgress(true);
        statusLabel.setText("Status: Processing");
    }

    @Override
    public void onConvertStarting(int total)
    {
        progressBar.setValue(0);
        progressBar.setMaximum(total);
    }

    @Override
    public void onConvertProgressing(int counter)
    {
        progressBar.setValue(counter);
    }

    @Override
    public void onInfo(String info)
    {
        appendInfo(info);
    }

    @Override
    public void onError(String message, Throwable throwable)
    {
        appendInfo(message + '\n' + AppUtils.toString(throwable));
    }

    @Override
    public void onFinish()
    {
        statusLabel.setText("Status: Done");
        appendInfo("Process done.");
        inProgress(false);
    }

    private synchronized void appendInfo(String text)
    {
        infoTa.append("[%s] %s\n".formatted(INFO_TA_DTF.format(ZonedDateTime.now()), text));
        infoTa.setCaretPosition(infoTa.getDocument().getLength());
    }

    static void main()
    {
        System.setProperty("apple.awt.application.name", "URL to Image Converter for Microsoft Excel");
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        if(Taskbar.isTaskbarSupported())
        {
            var taskbar = Taskbar.getTaskbar();

            if(taskbar.isSupported(Taskbar.Feature.ICON_IMAGE))
            {
                var iconUrl = MainFrame.class.getClassLoader().getResource("images/icon.png");

                if(iconUrl != null)
                {
                    taskbar.setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));
                }
            }
        }

        ZipSecureFile.setMaxFileCount(2000);

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
