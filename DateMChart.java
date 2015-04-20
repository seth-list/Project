
//import com.notUse.CategoryAxis;
import javafx.scene.chart.CategoryAxis;
import extfx.scene.chart.DateAxis;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;



/**
 * Created with IntelliJ IDEA.
 * User: Seth
 * Date: 17.04.14
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class DateMChart extends JPanel
{

    //панель, где помещается сцена
    private JFXPanel javafxPanel;

    //параметры для расчета координат квадрата увеличения
    //левый нижний угол и правый верхний
    private SimpleDoubleProperty rectinitX = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectinitY = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectX = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectY = new SimpleDoubleProperty();

    //кнопки для работы и сжатия
    private Button workButton;
    private Button compressButton;
    private Button decompButton;
    private Button criticalButton;

    private ScrollPane chart;
    //раскр списки для задач
    private ComboBox<String> beginCB;
    private ComboBox<String> endCB;

    //работы с датами в sql
    private Button plusButton;
    private Button minusButton;


    //sql class
    private String nameTask = "";
    //статусы изменения даты
    private int statusTask;

    //оси координат
    private DateAxis     xAxis;
    private CategoryAxis yAxis;



    //линии на графике
    private ObservableList<XYChart.Series<Date, String>> SData =
            FXCollections.observableArrayList();



    //восстановление range
    private List<String> range = new ArrayList<String>();

    //данные для оси категорий
    private ObservableList<String> cats =
            FXCollections.observableArrayList();

    //общие данные в графике в виде карты со множеством дат
    //показывает задачи, которые наследуются для данной
    //первая задача в графике ничего не наследует, узел состоит из одного значения
    private Map<cString, ArrayList<Date>> dataGraph
            = new LinkedHashMap<cString, ArrayList<Date>>();

    //текущие данные в графике в виде массива со множеством подмассивов
    private Map<cString, ArrayList<Date>> currentGraph
            = new LinkedHashMap <cString, ArrayList<Date>>();



    //названия графика
    private String titlex = "";

    //элементы для размещения на сцене
    private Rectangle zoomRect = new Rectangle();
    private BorderPane root = new BorderPane();
    private StackPane chartContainer = new StackPane();
    private XYChart.Series<Date, String> series;
    private boolean zoom = false;

    //предыдущие значения на оси координат
    private ObservableList<String> oldcats;

    private Date initXLowerBound = new Date();
    private Date  initXUpperBound = new Date();

    //переменная нового получения данных по работам а не по задачам
    private boolean work = false;


    //надпись
    private TextArea edit;

    //массив уже сделанных событий нажатий
    private ArrayList<ArrayList<Object>> atN = new ArrayList<ArrayList<Object>>();


    //массив данных в расскрывающихся списках
    private ObservableList<String> beginPath =
            FXCollections.observableArrayList();

    private ObservableList<String> endPath =
            FXCollections.observableArrayList();

    //выбранная задача
    private String selectTask;

    //сетка координат
    public   LineChart<Date, String> lineChart;

    //конструктор
    DateMChart()
    {
        //необходимо для того, чтобы после
        //переключения на другую страницу график не исчез
        Platform.setImplicitExit(false);
        //инициализация формы графика в потоке
        init();
        //установить у класса форму
        setLayout(new GridLayout(1,1,0,0));
        //добавить на JPanel - javafxPanel
        add(javafxPanel);
    }


    //инициализация и нажатие мыши
    public void init()
    {
        //панель для графика
        javafxPanel = new JFXPanel();

        // create JavaFX scene
        Platform.runLater(new Runnable()
        {
            public void run()
            {
                //создание сцены
                createScene();

                lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED,

                        new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent e)
                            {
                                if(e.getButton().equals(MouseButton.PRIMARY))
                                {


                                    //блокировать кнопки >> и <<
                                    plusButton.setDisable(true);
                                    minusButton.setDisable(true);
                                    //блокировать
                                    workButton.setDisable(true);

                                    //получение координат точки щелчка
                                    ArrayList<Object> point = getPoint(e);
                                    if(point==null) return;

                                    //координаты точки
                                    Date date = (Date)point.get(0);
                                    String value = (String)point.get(1);
                                    //========================================
                                    if(work)
                                    {
                                        chooseWork(date, value);
                                        return;
                                    }

                                    String name;
                                    if(value.contains(" - \n"))
                                    {
                                        String[] names = value.split("<")[0].split(" - \\n");
                                        name = names[0]+" - \n"+ names[1];
                                    }
                                    else
                                    {
                                        name = value;
                                    }
                                    //весь массив дат
                                    ArrayList<Date> allNode = currentGraph.get(new cString(name));
                                    //получение даты начала и окончание, а также промежут. дат
                                    //количество дат
                                    //начальная дата
                                    Date date1 = Collections.min(allNode);
                                    //окончательная дата
                                    Date date2 = Collections.max(allNode);

                                    //массив промежуточных дат
                                    ArrayList<Date> middleNode = new ArrayList<Date>();

                                    //получение промежуточных узлов
                                    for(Date dates : allNode)
                                    {
                                        if(!dates.equals(date1)&&!dates.equals(date2))
                                        {
                                            middleNode.add(dates);
                                        }
                                    }


                                    //то получаем даты для точки
                                    //если дата на графике при нажатии мыши меду датами задачи
                                    if(dateWork.dateBetweenDates(date, date1, date2))
                                    {

                                        //добавление надписи
                                        annotation(value, date1, date2,
                                                date, middleNode);

                                        selectTask = value;

                                        if(e.getClickCount() == 2) //двойной щелчок
                                        {
                                            //функция двойного щелчка
                                            doDoubleClick(selectTask);

                                            compressButton.setDisable(false);
                                            decompButton.setDisable(true);
                                            //сортировка
                                            currentGraph = cString.sortingMap(currentGraph);
                                            //добавление значений
                                            addAllValue(currentGraph);
                                        }

                                    }

                                }
                            }
                        });
            }
        });
    }

    //установить название графиков
    public void setTitle(String t)
    {
        titlex = t;
    }


    //данные очистить
    public void dataClear()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                //очистить весь массив данных
                SData.clear();
                //очистить все списки данных
                //данные, которые взяты из базы данных, по умолчанию
                dataGraph.clear();
                //данные которые получаются после сокращения
                //вначале равные dataGraph
                currentGraph.clear();
                atN.clear();
                //очистить раскрывающиеся списки
                beginPath.clear();
                endPath.clear();
                //очистить категории на графике
                cats.clear();
                range.clear();
                //флаги для того, чтобы установить
                //или не установить сокр. имена
            }
        });
    }


    //установка размера графика
    private void setSizeGraph()
    {

        chart.setFitToHeight(true);
        int size = range.size();
        if(size>10)
        {
            chart.setFitToHeight(false);
            int height = size*40;
            lineChart.setPrefHeight(height);
        }
    }


    //TODO восстановление надо бы несколько раз проходило бы
    //создание сцены
    private void createScene()
    {

        //ось игрек и ось икс
        yAxis = new CategoryAxis();
        xAxis = new DateAxis();

        //Создание графика
        lineChart = new LineChart<Date,String>(xAxis,yAxis, SData);
        lineChart.setTitle(titlex);

        //установление названий для осей координат
        xAxis.setLabel("Дата");
        yAxis.setLabel("Задача");

        //невидимая легенда
        lineChart.setLegendVisible(true);
        lineChart.setMinWidth(0);
        lineChart.setLegendVisible(false);


        chart = new ScrollPane();

        chart.setContent(lineChart);
        chart.setFitToHeight(true);
        chart.setFitToWidth(true);


        //добавить в центр график
        chartContainer.getChildren().add(chart);


        //zoomator - квадрат для увеличения
        zoomRect.setManaged(false);
        zoomRect.setFill(Color.LIGHTYELLOW.deriveColor(0, 1, 1, 0.5));
        zoomRect.setStroke(Color.ROSYBROWN);
        zoomRect.setStrokeDashOffset(50);

        //добавить квадрат на фон
        chartContainer.getChildren().add(zoomRect);
        //приготовить для увеличения
        setUpZooming(zoomRect, lineChart);

        //кнопки для управления видом графика
        //кнопки - увеличения
        final Button resetButton = new Button("Reset");
        workButton = new Button("Список работ");
        compressButton = new Button("Сжать");
        decompButton = new Button("Восстановить");

        criticalButton = new Button("Критический путь");
        Button beginButton = new Button("Начальный вид");
        Button zoomButton = new Button("Zoom");


        //раскр списки для задач
        beginCB = new ComboBox<String>();
        endCB   = new ComboBox<String>();
        //работы с датами в sql
        plusButton = new Button(">>");
        minusButton = new Button("<<");
        //надпись
        edit = new TextArea();


        //панель под все кнопки
        final HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        //Панель под вертикальные кнопки
        final VBox resize = new VBox();
        resize.setPadding(new Insets(10));
        resize.setAlignment(Pos.CENTER);
        resize.setSpacing(8);
        resize.getStyleClass().add("vbox");
        resize.setId("vbox-custom");

        //Панель под вертикальные кнопки
        final VBox critical = new VBox();
        critical.setPadding(new Insets(10));
        critical.setAlignment(Pos.CENTER);
        critical.setSpacing(8);
        critical.getStyleClass().add("vbox");
        critical.setId("vbox-custom");

        //Панель под вертикальные кнопки
        final VBox cbs = new VBox();
        cbs.setPadding(new Insets(10));
        cbs.setAlignment(Pos.CENTER);
        cbs.setSpacing(8);
        cbs.getStyleClass().add("vbox");
        cbs.setId("vbox-custom");

        //Панель под вертикальные кнопки
        final VBox zoomer = new VBox();
        zoomer.setPadding(new Insets(10));
        zoomer.setAlignment(Pos.CENTER);
        zoomer.setSpacing(8);
        zoomer.getStyleClass().add("vbox");
        zoomer.setId("vbox-custom");

        //Панель под вертикальные кнопки
        final VBox plmin = new VBox();
        plmin.setPadding(new Insets(10));
        plmin.setAlignment(Pos.CENTER);
        plmin.setSpacing(8);
        plmin.getStyleClass().add("vbox");
        plmin.setId("vbox-custom");

        //два раскрывающихся списка
        beginCB.setItems(beginPath);
        endCB.setItems(endPath);
        endCB.setPrefWidth(100);
        beginCB.setPrefWidth(100);

        //кнопки сжатия и восстановления
        compressButton.setDisable(false);
        decompButton.setDisable(true);

        //получение списка работ для данной задачи
        workButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                //получить массив в виде дерева
                Map<cString, ArrayList<Date>> workGraph =
                        getWorkGraph();

                if(workGraph.size()==0)
                {
                    String msg = "Работ в данной задаче еще нет!";
                    JLabel msgLabel = new JLabel(msg, JLabel.CENTER);
                    JOptionPane.showMessageDialog(new JPanel(), msgLabel, "Input Error",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                edit.clear();
                //очистить график
                SData.clear();
                lineChart.getData().clear();
                lineChart.setAnimated(false);
                //добавить проект
                //добавить на график
                workGraph = cString.sortingMap(workGraph);
                addAllValue(workGraph);
                //блокировать кнопки
                criticalButton.setDisable(true);
                compressButton.setDisable(true);
                decompButton.setDisable(true);
                plusButton.setDisable(true);
                minusButton.setDisable(true);
                workButton.setDisable(true);
                work = true;

            }
        });

        //получение критического пути !!!
        criticalButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                try
                {
                    //получения данных из раскрывающихся список
                    String end = endCB.getSelectionModel().getSelectedItem();
                    String beg = beginCB.getSelectionModel().getSelectedItem();
                    //работает - находит критический путь
                    shortestPath sh = new shortestPath(dataGraph);
                    Map<String, Object> path = sh.findPath(end, beg);
                    //если пути нет между двумя точками
                    if(path==null)
                    {
                        String msg = "Критического пути между задачами не существует!";
                        JLabel msgLabel = new JLabel(msg, JLabel.CENTER);
                        JOptionPane.showMessageDialog(new JPanel(), msgLabel, "Input Error",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    //получить массив в виде дерева
                    Map<cString, ArrayList<Date>> criticalGraph =
                            getCriticalPath(path);
                    //флаги, чтобы не сокращать
                    //очистить график
                    SData.clear();
                    lineChart.getData().clear();
                    lineChart.setAnimated(false);

                    //даты
                    ArrayList<Object> project = MnForm.sql.selectValue(778).get(0);
                    //получение начала и окнончания проекта
                    String projectName = project.get(0).toString();
                    Date begin = (Date)project.get(3);
                    Date ending = (Date)project.get(4);
                    ArrayList<Date> dates = new ArrayList<Date>();
                    dates.add(begin);
                    dates.add(ending);
                    //проект
                    //добавить проект в сокращенном виде
                    criticalGraph.put(new cString(projectName), dates);
                    //добавить на график
                    criticalGraph = cString.sortingMap(criticalGraph);
                    addAllValue(criticalGraph);
                    //блокировать кнопки
                    criticalButton.setDisable(true);
                    compressButton.setDisable(true);
                    decompButton.setDisable(true);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });


        //приведение к начальному виду
        beginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                lineChart.setAnimated(false);
                SData.clear();
                lineChart.getData().clear();
                compressButton.setDisable(false);
                decompButton.setDisable(true);
                criticalButton.setDisable(false);
                //восстановление по dataGraph
                dataGraph = cString.sortingMap(dataGraph);
                addAllValue(dataGraph);
                currentGraph.clear();
                currentGraph = new LinkedHashMap<cString, ArrayList<Date>>(dataGraph);
                work = false;
            }
        });

        //сжатие
        compressButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                //сжатие

                changeSize(false);
                compressButton.setDisable(true);
                decompButton.setDisable(false);
            }
        });

        //восстановить
        decompButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent)
            {

                //восстановление
                changeSize(true);
                compressButton.setDisable(false);
                decompButton.setDisable(true);
            }
        });


        //добавить две кнопки
        resize.getChildren().addAll(compressButton, decompButton);

        //добавить две кнопки
        critical.getChildren().addAll(criticalButton, beginButton);



        //панели заполнить кнопками
        cbs.getChildren().addAll(beginCB, endCB);
        zoomer.getChildren().addAll(zoomButton, resetButton);
        plmin.getChildren().addAll(minusButton, plusButton);


        //блокировать кнопки до проверки
        plusButton.setDisable(true);
        minusButton.setDisable(true);
        //блокировать
        workButton.setDisable(true);

        //styles
        //стиль для кнопок
        plusButton.setId("dark-grey");
        minusButton.setId("dark-grey");
        workButton.setId("dark-grey");
        zoomButton.setId("dark-grey");
        resetButton.setId("dark-grey");
        compressButton.setId("dark-grey");
        decompButton.setId("dark-grey");
        criticalButton.setId("dark-grey");
        beginButton.setId("dark-grey");


        //обработчик
        minusButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {

                //изменить в sql данные
                changeDays(-1);
                //изменить на графике данные
                newDate(-1);
                //обнулить данные и график
                edit.setText("");
                oldcats = yAxis.getCategories();
                //восстановить значение
                resetClick();
            }
        });

        //обработчик
        plusButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {

                //изменить в sql
                changeDays(1);
                //поставить новые данные на графике
                newDate(1);
                edit.setText("");
                oldcats = yAxis.getCategories();
                resetClick();
            }
        });


        zoomButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                compressButton.setDisable(true);
                decompButton.setDisable(true);
                //сделать увеличение
                if (!zoom) {
                    initXLowerBound = xAxis.getLowerBound();
                    initXUpperBound = xAxis.getUpperBound();
                    zoom = true;
                }
                doZoom(zoomRect, lineChart);
            }
        });

        //reset
        resetButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {

                if(zoom)
                {
                    //восстановить значение
                    resetClick();
                }
            }
        });



        //если увеличение очень маленькое, то не делать его
        final BooleanBinding disableControls =
                zoomRect.widthProperty().lessThan(5)
                        .or(zoomRect.heightProperty().lessThan(5));

        zoomButton.disableProperty().bind(disableControls);

        //====================================
        //для текстового поля параметры
        edit.setPrefRowCount(10);
        edit.setPrefColumnCount(100);
        edit.setWrapText(true);
        edit.setPrefWidth(175);
        edit.setMaxHeight(60);
        edit.setEditable(false);
        //------------------------------------

        //стиль для панели кнопок управления
        controls.getStyleClass().add("pane");
        //=====================================
        controls.getChildren().addAll(zoomer, edit, plmin,
                workButton, resize, critical, cbs);

        //установить по центру график
        root.setCenter(chartContainer);
        //внизу кнопки управления
        root.setBottom(controls);


        //создание сцены
        Scene scene = new Scene(root, 700, 430);
        /*
        //установить css стили для сцены
        scene.getStylesheets().add(
                getClass().getResource("css/graph.css").toExternalForm()
        );
        */
        //на панель установить сцену
        javafxPanel.setScene(scene);

    }


    public void addXFiles(final Map<cString ,ArrayList<Date>> map)
    {

        // create JavaFX scene
        Platform.runLater(new Runnable()
        {
            public void run()
            {

                for (Map.Entry<cString, ArrayList<Date>> entry : map.entrySet())
                {

                    ArrayList<Date> date = entry.getValue();
                    String task = entry.getKey().getChild();
                    //если первый раз загружается, то загружаются первоначальные значения
                    //в дерево
                    //String value = setGrName(task);
                    String value = task;

                    //заполнение раскр. списков задачами
                    beginPath.add(value);
                    endPath.add(value);

                    //добавляем полное имя в список полных имен
                    //fullName.add(task);

                    //данные о датах в задаче
                    ArrayList<Date> dateData = new ArrayList<Date>(date);
                    ArrayList<Date> dateCurrent = new ArrayList<Date>(date);

                    //в карту узлов добавляю корневой узел
                    dataGraph.put(new cString(task), dateData);
                    currentGraph.put(new cString(task), dateCurrent);

                    //данные о нажатии узла
                    ArrayList<Object> pressATN = new ArrayList<Object>();
                    pressATN.add(task); //узел
                    pressATN.add(false); //не нажат
                    //добавление
                    atN.add(pressATN);
                }
            }
        });

    }

    public void alienXFiles(final Map<cString ,ArrayList<Date>> map)
    {
        // create JavaFX scene
        Platform.runLater(new Runnable()
        {
            public void run()
            {
                for (Map.Entry<cString, ArrayList<Date>> entry : map.entrySet())
                {

                    String task1 = entry.getKey().getChild();
                    //дата начала первой задачи в соединении
                    Date old = dataGraph.get(new cString(task1)).get(0);

                    //данные о датах в задаче
                    ArrayList<Date> dateCross = new ArrayList<Date>();
                    dateCross.add(entry.getValue().get(0));
                    dateCross.add(entry.getValue().get(1));
                    dateCross.add(old);

                    //в карту узлов добавляю соединение
                    dataGraph.put(entry.getKey(), dateCross); //добавить предыдущую
                    currentGraph.put(entry.getKey(), dateCross); //добавить предыдущую
                }
            }
        });
    }


    public void addData(final Map<cString, ArrayList<Date>> map)
    {

        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                int size = map.size();
                //HashSet<String> set = new HashSet<String>();

                for (Map.Entry<cString, ArrayList<Date>> entry : map.entrySet())
                {

                    String task = entry.getKey().getChild();
                    ArrayList<Date> date = entry.getValue();

                    String value;
                    if(task.contains(" - \n"))
                    {
                        String[] names = task.split(" - \\n");
                        value = names[0]+" - \n"+
                                names[1];
                    }
                    else
                    value = task;

                    //выбрать первое значение в раскрывающихся списках
                    beginCB.getSelectionModel().selectFirst();
                    endCB.getSelectionModel().selectFirst();

                    //создать линию на графике
                    series = new XYChart.Series<Date, String>();

                    //установка имени графика
                    //установить имя
                    series.setName(task);

                    //поставить точку на графике
                    int on = 0;
                    while(on<date.size())
                    {
                        series.getData().add(new XYChart
                                .Data<Date, String>(date.get(on), value));
                        on++;
                    }


                    //добавить данные о графике
                    if(!SData.contains(series))
                    {
                        SData.add(series);
                    }
                    //=========================================
                    //исправление багов javafx!!!

                    if(!range.contains(series.getName()))
                    {
                        range.add(series.getName());
                    }
                    /*
                    if(!cats.contains(value))
                    {
                        cats.add(value);
                    }
                    */
                    //===========================================
                    //taskSize++;
                    //строка прогресс
                    //MnForm.progressBar.setValue(Math.round(taskSize/size*50));

                }

                Map<cString, ArrayList<Date>> sMap = cString.sortingMap(map);


                for (cString key : sMap.keySet())
                {
                    String uniqueName;
                    if(key.getChild().contains(" - \n"))
                    {
                        String[] names = key.getChild().split(" - \\n");
                        uniqueName = names[0]+" - \n"+names[1];
                    }
                    else
                    uniqueName = key.getChild();
                    /*
                    int i = 1;
                    while(cats.contains(uniqueName))
                    {
                        //если содержит (*)
                        uniqueName = uniqueName.replaceAll("\\(\\d+\\)", "");
                        uniqueName = uniqueName + String.format("(%s)", i);
                        i++;
                    }
                    */
                    cats.add(uniqueName);
                }



                yAxis.invalidateRange(range);
                yAxis.setCategories(cats);
                setSizeGraph();
            }
        });
    }


    //добавить данные для соединительных линий задач
    public void addCross(final Map<cString ,ArrayList<Date>> map)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (Map.Entry<cString, ArrayList<Date>> entry : map.entrySet())
                {

                    String task1 = entry.getKey().getChild();
                    String task2 = entry.getKey().getParent().split(">")[0];
                    Date date1 = entry.getValue().get(0);
                    Date date2 = entry.getValue().get(1);

                    String value1;
                    String value2;

                    if(task1.contains(" - \n"))
                    {
                        String[] names = task1.split(" - \\n");
                        value1 = names[0]+" - \n"+names[1];
                    }
                    else
                    value1 = task1;

                    if(task2.contains(" - \n"))
                    {
                        String[] names = task2.split(" - \\n");
                        value2 = names[0]+" - \n"+names[1];
                    }
                    else
                    value2 = task2;

                    series = new XYChart.Series<Date, String>();
                    series.setName(value1+"---"+value2);

                    series.getData().add(new XYChart.Data<Date, String>(date1, value1));
                    series.getData().add(new XYChart.Data<Date, String>(date2, value2));

                    SData.add(series);
                    //connSize++;
                    //строка прогресс
                    //MnForm.progressBar.setValue(50+Math.round(connSize/size*50));

                }
            }
        });
    }

    //получение координат точки по нажатию
    private ArrayList<Object> getPoint(MouseEvent e)
    {

        ArrayList<Object> point = new ArrayList<Object>();
        //очистить edit
        edit.clear();
        //выбирается фон
        Node chartBackground = lineChart.lookup(".chart-plot-background");

        //смещение относительно начала координат
        final double shiftX = getSceneShiftX(chartBackground);
        final double shiftY = getSceneShiftY(chartBackground);

        //координаты точки по нажатию мыши
        double x = e.getSceneX() - shiftX;
        double y = e.getSceneY() - shiftY;

        //все списки задач из оси игрек
        ObservableList<String> cat = yAxis.getCategories();

        //количество задач
        int size = cat.size();

        //размер оси y
        double scale = yAxis.getHeight();

        //размер участка
        double tickSize = scale/size;


        double num1 = (scale-y)/tickSize;
        int num2 = (int)num1;

        //получаем данные точки
        Date date = xAxis.getValueForDisplay(x);
        point.add(date);

        //проверка на вхождение в массив
        String value = "";
        if(num2<cat.size())
        {
            //дата которую щелкнули
            value = cat.get(num2);
        }
        else return null;
        point.add(value);
        return point;

    }


    //событие по двойному щелчку
    private boolean doDoubleClick(String clickData)
    {

        //очистка данных графика
        SData.clear();
        lineChart.getData().clear();

        //выключить анимацию графика
        lineChart.setAnimated(false);

        //не работать с уже сокращенными !!! а то ппц
        //TODO другой признак ???
        if(clickData.contains(" - \n")) return false;


        ArrayList<String> split = new ArrayList<String>();
        for(cString key : currentGraph.keySet())
        {
            if(key.getParent().equals(clickData))
            {
                split.add(key.getChild());
            }
        }

        for(cString key : currentGraph.keySet())
        {
            if(split.contains(key.getParent()))
            {
                if(key.getChild().contains(" - \n")) return false;
            }
        }

        //событие нажатия графика
        ArrayList<Object> click = new ArrayList<Object>();
        click.add(clickData);
        click.add(true);

        //если среди событий нажатия такой есть
        //то есть уже нажато, то восстанавливается значение
        if(atN.contains(click))
        {
            //преобразовать currentGraph
            //c учетом dataGraph
            //в значении clickData
            //возвращение значений
            intercept(clickData);

            //находится индекс события ненажатия
            int ret = atN.indexOf(click);
            //изменяется события на нажатое
            atN.get(ret).remove(1);
            atN.get(ret).add(false);

            return true;   //нажимать снова не надо

        }
        //иначе
        else
        {
            int  sizePrevTask = 0;
            //проходит по всем предыдущим задачам
            for (cString entry : currentGraph.keySet())
            {
                 if(entry.checkRoot()&&entry
                         .getParent().equals(clickData))
                 {
                     sizePrevTask++;
                     if(entry.getChild().contains(" - \n"))
                     //если одна из предыдущих уже сокращена
                     return false; //не сокращается
                 }
            }
            if(sizePrevTask<2) return false; //если нечего сокращать
            //то не сокращается

            //создается событие ненажатия
            click.remove(1);
            click.add(false);
            //находится индекс события ненажатия
            int ret = atN.indexOf(click);
            if(ret!=-1)
            {
                //изменяется события на нажатое
                atN.get(ret).remove(1);
                atN.get(ret).add(true);
            }
            else return false;

            //подаем dataGraph.get(i)
            changeData(clickData);  //!!!
        }


        return true;
    }


    //получение описания работы
    private void annWork(String name, String worker, Date date1, Date date2, String status)
    {
        edit.appendText("Работа: "+name+"\n");
        edit.appendText("Рабочий: "+worker+"\n");
        edit.appendText("Дата начала: "+date1.toString()+"\n");
        edit.appendText("Дата окончания: "+date2.toString()+"\n");
        edit.appendText("Статус выполнения: "+status);
        edit.positionCaret(0);
    }


    //надпись по щелчку мыши
    private void annotation(String clickData, Date date1,
                            Date date2, Date date,
                            ArrayList<Date> middleNode)
    {
        //получается полное имя по сокращенному
        String[] arrStr = clickData.replace("\n","").split(" - ");
        String fullName = "";

        int zero = 0;
        //это нужно для получения полного названия сокращенной группы задач
        while (zero<arrStr.length)
        {
            if(zero>=1)
                fullName = fullName + " - ";
            //fullName = fullName+getFullName(arrStr[zero]);
            fullName = fullName+arrStr[zero];
            zero++;
        }

        //блокировать кнопки >> и <<
        plusButton.setDisable(true);
        minusButton.setDisable(true);

        boolean middle = false;
        for (Date aMiddleNode : middleNode)
        {
            if (dateWork.approxDate(aMiddleNode, date)) {
                edit.appendText("Средний узел: " + fullName + "\n");
                edit.appendText("Дата: " + aMiddleNode
                        .toString() + "\n");
                middle = true;
            }
        }

        //-------------------------------------
        //если начальная дата
        if(dateWork.approxDate(date1,date))
        {
            edit.appendText("Начало задачи: "+fullName+"\n");
            edit.appendText("Дата: "+date1.toString()+"\n");
            if(middleNode.size()==0)
            {
                nameTask = fullName;
                //разблокировать кнопки
                //статус показывает что перемещается
                // -1 - начало
                //  1  - конец
                statusTask = -1;
                treatRight(date1);
            }

        }
        else
            //если конечная дата
            if(dateWork.approxDate(date2,date))
            {

                edit.appendText("Окончание задачи: "+fullName+"\n");
                edit.appendText("Дата: "+date2.toString()+"\n");
                if(middleNode.size()==0)
                {
                    nameTask = fullName;
                    statusTask = 1;
                    //разблокировать кнопки
                    treatRight(date2);
                }
            }
            //если просто внутри
            else
            {
                //Добавить текст
                if(!middle)
                {
                    edit.appendText(fullName+"\n");
                    edit.appendText("Дата начала: "+date1.toString()+"\n");
                    edit.appendText("Дата окончания: "+date2.toString());
                    //разблокировать
                    workButton.setDisable(false);
                }
            }


        edit.positionCaret(0);
    }



    //ALTER TABLE report add idRep INT NOT NULL AUTO_INCREMENT PRIMARY KEY
    //ALTER TABLE stage ADD FOREIGN KEY (idDocs) REFERENCES docs(idDocs) ON DELETE CASCADE ON UPDATE CASCADE;

    //изменение данных после щелчка
    private void changeData(String click)
    {

        //1) - очистка данных графика
        SData.clear();
        lineChart.getData().clear();

        //имя сокращенных задач
        StringBuilder split = new StringBuilder();
        //массив дат сокращенных задач
        ArrayList<Date> datesPrev = new ArrayList<Date>();
        //массив названий сокращенных задач
        ArrayList<String> taskZip = new ArrayList<String>();

        int i = 0;
        //пройтись по всем предыдущим задачам
        String name = "";
        for (cString key : currentGraph.keySet())
        {
            if(key.checkRoot()&&key.getParent().equals(click))
            {
                name = key.getChild();
                if(i==0)
                {
                    split.append(name);
                    split.append(" - \n");
                }
                taskZip.add(name);
                i++;
            }
        }

        //имя сокращенных задач
        //название замены - имя первой задачи замены
        // - имя последней задачи замены
        String nameSplit = split.append(name).toString();

        //промежуточная карта, чтобы избежать concurrentException
        Map<cString, ArrayList<Date>> middleMap
                = new LinkedHashMap<cString, ArrayList<Date>>(currentGraph);


        int j = 0;
        for (Map.Entry<cString, ArrayList<Date>> entry : middleMap.entrySet())
        {

            //получить все соединения с текущей задачей
            if(entry.getKey().checkRoot()&&entry.getKey()
                    .getParent().equals(click))
            {
                //нахождение даты начала предыдущей задачи (old)
                Date date1 = entry.getValue().get(2);
                //нахождение даты начала связи - окончание задачи
                Date date2 = entry.getValue().get(0);
                //добавить в массив
                if(!datesPrev.contains(date1))
                datesPrev.add(date1);
                if(!datesPrev.contains(date2))
                datesPrev.add(date2);

                //удаление задач, которые сокращаются
                currentGraph.remove(new cString(entry
                        .getKey().getChild()));
                i++;
            }
            //если это соединение для текущих
            if(taskZip.contains(entry.getKey()
                    .getChild())&&entry.getKey().checkRoot())
            {
                //установление сокращенной задачи в качестве Child
                //для соединений с текущей задачей
                ArrayList<Date> datesPrevConnect = entry.getValue();
                String next = entry.getKey().getParent();
                currentGraph.remove(entry.getKey());
                currentGraph.put(new cString(nameSplit+"<"+j, next),datesPrevConnect);
                j++;
            }
            //если это соединения для сокращенных
            if(taskZip.contains(entry.getKey()
                    .getParent())&&entry.getKey().checkRoot())
            {
                //установление сокращенной задачи в качестве Parent
                //для соединений с сокращенной задачей
                ArrayList<Date> datesPrevConnect = entry.getValue();
                String next = entry.getKey().getChild();
                currentGraph.remove(entry.getKey());
                currentGraph.put(new cString(next, nameSplit),datesPrevConnect);
            }
        }

        //в новый массив добавляется сокращенная задача
        currentGraph.put(new cString(nameSplit), datesPrev);

    }

    //обновление старых значений
    private void intercept(String data)
    {

        //1) - очистка данных графика
        SData.clear();
        lineChart.getData().clear();


        ArrayList<String> needRestoration = new ArrayList<String>();
        ArrayList<String> zipValue = new ArrayList<String>();

        //пройтись по всем предыдущим задачам
        String nameSplit = "";
        for (cString key : currentGraph.keySet())
        {
            if(key.getParent().equals(data))
            {
                nameSplit = key.getChild().split("<")[0];
            }
        }

        //массив предудущих задач
        for (cString key : dataGraph.keySet())
        {
            if(key.getParent().equals(data))
            {
                zipValue.add(key.getChild());
            }
        }

        //удаление сокращенного узла
        currentGraph.remove(new cString(nameSplit));
        //промежуточная карта, чтобы избежать concurrentException
        Map<cString, ArrayList<Date>> middleMap
                = new LinkedHashMap<cString, ArrayList<Date>>(currentGraph);



        for (Map.Entry<cString, ArrayList<Date>> entry : middleMap.entrySet())
        {

            //получить все соединения с сокращенной задачей
            if(entry.getKey().getChild().split("<")[0].equals(nameSplit))
            {
                if(!needRestoration.contains(entry.getKey().getParent()))
                needRestoration.add(entry.getKey().getParent());
                //удаление предыдущих соединений
                currentGraph.remove(entry.getKey());
            }
            //получить все соединения с сокращенной задачей
            if(entry.getKey().getParent().equals(nameSplit))
            {
                if(!needRestoration.contains(entry.getKey().getChild()))
                needRestoration.add(entry.getKey().getChild());
                //удаление предыдущих соединений
                currentGraph.remove(entry.getKey());
            }
        }

        for (Map.Entry<cString, ArrayList<Date>> entry : dataGraph.entrySet())
        {
              String parent = entry.getKey().getParent();
              String child  = entry.getKey().getChild();
              if(needRestoration.contains(parent))
              {
                 //в новый массив добавляется последующее соединение
                 currentGraph.put(entry.getKey(), entry.getValue());
              }
              if(needRestoration.contains(child))
              {
                 //в новый массив добавляется последующее соединение
                 currentGraph.put(entry.getKey(), entry.getValue());
              }
              if(zipValue.contains(child) && entry.getKey().getParent().equals(""))
              {
                //в новый массив добавляется задача
                currentGraph.put(entry.getKey(), entry.getValue());
              }
        }

    }

    //добавить массив значений на графики
    private void addAllValue(Map<cString, ArrayList<Date>> data)
    {
        cats.clear();
        range.clear();

        Map<cString, ArrayList<Date>> map1
                = new LinkedHashMap<cString, ArrayList<Date>>();

        Map<cString, ArrayList<Date>> map2
                = new LinkedHashMap<cString, ArrayList<Date>>();

        int signConnect = 0;
        for (Map.Entry<cString, ArrayList<Date>> entry : data.entrySet())
        {
            if(!entry.getKey().checkRoot())
            {
                //добавление задач
                String task = entry.getKey().getChild();
                ArrayList<Date> dates = entry.getValue();
                map1.put(new cString(task), dates);
            }
            else
            {
                //int size = data.size() - sizeChildren;
                String child = entry.getKey().getChild().split("<")[0];
                map2.put(new cString(child,entry.getKey().getParent()+">"+signConnect),
                        entry.getValue());
                signConnect++;
            }
        }

        //добавления графика
        addCross(map2);
        //добавления графика
        addData(map1);

    }



    //получить дату начала и окончания для данной задачи
    private ArrayList<Date> getAccelDate(String name)
    {
        ArrayList<Date> dates = new ArrayList<Date>();
        Date date1 = dataGraph.get(new cString(name)).get(0);
        Date date2 = dataGraph.get(new cString(name)).get(1);
        dates.add(date1);
        dates.add(date2);
        return dates;
    }


    //выбор работы по щелчку
    private void chooseWork(Date date, String work)
    {
        //получение списка работ по id
        try
        {
            ArrayList<ArrayList<Object>> works = MnForm.sql.selectValue(8);

            int on=0;
            while(on<works.size())
            {
                //сокращенное имя работы + имя работника
                String name = works.get(on).get(2)+
                        "\n("+works.get(on).get(5)+")";
                String workFName = (String)works.get(on).get(2);
                String worker = (String)works.get(on).get(5);

                Date date1 = (Date)works.get(on).get(0);
                Date date2 = (Date)works.get(on).get(1);
                String status = (String)works.get(on).get(4);

                //если щелчок куда надо
                if(name.equals(work)&&dateWork.dateBetweenDates(date, date1, date2))
                {
                    annWork(workFName, worker, date1, date2, status);
                    break;
                }

                on++;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    //получение списка работ в задаче
    private Map<cString, ArrayList<Date>> getWorkGraph()
    {
        //критический путь
        Map<cString, ArrayList<Date>> workGraph
                = new HashMap<cString, ArrayList<Date>>();
        try
        {

            MnForm.sql.setTypeTask(selectTask);
            int id = (Integer)MnForm.sql.selectValue(73).get(0).get(0);
            MnForm.sql.setIdTask(id);
            //получение списка работ по id
            ArrayList<ArrayList<Object>> works = MnForm.sql.selectValue(8);

            int on=0;
            while(on<works.size())
            {
                //сокращенное имя работы + имя работника
                String name = works.get(on).get(2)+
                        "\n("+works.get(on).get(5)+")";
                Date date1 = (Date)works.get(on).get(0);
                Date date2 = (Date)works.get(on).get(1);

                ArrayList<Date> dates = new ArrayList<Date>();
                dates.add(date1);
                dates.add(date2);
                workGraph.put(new cString(name), dates);
                on++;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return workGraph;
    }

    //получение критического пути
    private Map<cString, ArrayList<Date>> getCriticalPath(Map<String, Object> path)
    {
        //критический путь
        Map<cString, ArrayList<Date>> criticalGraph
                = new HashMap<cString, ArrayList<Date>>();

        //получить корень
        String root = path.get("point").toString();
        //получение даты начала и конца
        ArrayList<Date> dates = getAccelDate(root);
        criticalGraph.put(new cString(root), dates);

        //цикл пока путь не закончится
        while(true)
        {
            //цикл пока следующего назначения нет
            if(!path.containsKey("from"))
            {
                break;
            }
            path = (Map<String, Object>)path.get("from");
            //данные о пред узле
            String prev = root;
            Date date = dates.get(1);

            //получить корень
            root = path.get("point").toString();

            //получение даты начала и конца
            dates = getAccelDate(root);
            criticalGraph.put(new cString(root), dates);

            //соединение
            //данные о соед. крышке
            ArrayList<Date> nextData = new ArrayList<Date>();
            nextData.add(date);
            //получение даты начала и конца
            nextData.add(dates.get(0));
            //=======================================
            criticalGraph.put(new cString(prev, root), nextData);
        }

        return criticalGraph;
    }


    //восстановление после zoom
    private void resetClick()
    {
        compressButton.setDisable(false);
        decompButton.setDisable(false);
        zoom = false;
        //восстановление после zoom
        DateAxis xAxis = (DateAxis)lineChart.getXAxis();


        if(!initXLowerBound.equals(initXUpperBound))
        {
            xAxis.setLowerBound(initXLowerBound);
            xAxis.setUpperBound(initXUpperBound);

            ArrayList<Date> dateList = new ArrayList<Date>();
            dateList.clear();
            while(!initXLowerBound.equals(initXUpperBound))
            {
                dateList.add(initXLowerBound);
                initXLowerBound = dateWork.addDays(initXLowerBound,1);
            }
            dateList.add(initXLowerBound);
            xAxis.invalidateRange(dateList);

        }

        //восстановить ось координат
        CategoryAxis yAxis = (CategoryAxis)lineChart.getYAxis();
        yAxis.invalidateRange(oldcats);

        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);

        zoomRect.setWidth(0);
        zoomRect.setHeight(0);
    }


    //сжать или расширить график
    private void changeSize(boolean howto)
    {

        Map<cString, ArrayList<Date>> middleMap
                = new LinkedHashMap<cString, ArrayList<Date>>(currentGraph);

        for (cString key : middleMap.keySet())
        {
            //событие нажатия графика
            ArrayList<Object> click = new ArrayList<Object>();
            click.add(key.getChild());
            click.add(howto);

            if(atN.contains(click))
            {
                doDoubleClick(key.getChild());
            }

        }
        addAllValue(currentGraph);
    }


    //получить дату предыдущих задач
    private Date getEndPrev(String nameTask)
    {
        //Дата пред задачи
        Date endPrev = null;

        try
        {
            ArrayList<ArrayList<Object>> tasks = MnForm.sql.selectValue(7);
            int i = 0;
            while (i<tasks.size())
            {
                //имя последующей задачи
                //если имя последующей задачи - tasks.get(i).get(5)
                //совпадает с текущей задаче, то получаем дату
                //если она больше то присваиваем
                List<Object> ends = tasks.get(i)
                        .subList(5, tasks.get(i).size());

                if(ends.contains(nameTask))
                {
                    //дата окончания
                    Date endDate = (Date)tasks.get(i).get(2);
                    if(endPrev==null||endDate.after(endPrev))
                    {
                        endPrev = endDate;
                    }

                }
                i++;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //возвращение
        return endPrev;
    }


    //проверка кнопок на способность работы
    private void treatRight(Date newDate)
    {
        try
        {
            //устанавливаю имя задачи
            MnForm.sql.setTypeTask(nameTask);

            //id следующей задачи
            ArrayList<ArrayList<Object>> curse = MnForm.sql.selectValue(73);

            if(curse.isEmpty()) return;

            ArrayList<Object> curTask = curse.get(0);

            int id = (Integer)curTask.get(0);
            //получение id текущей задачи
            MnForm.sql.setIdTask(id);

            //id's последующих задач
            ArrayList<ArrayList<Object>> nexts
                    = MnForm.sql.selectValue(711);


            //даты
            ArrayList<Object> project
                    = MnForm.sql.selectValue(778).get(0);

            //получение начала и окнончания проекта
            Date begNext = (Date)project.get(4);

            //получение пред даты
            Date endPrev = getEndPrev(nameTask);

            //если последующая задача пустая
            if(endPrev==null)
            {
                endPrev = (Date)project.get(3);
            }

            if(nexts!=null)
            {
                for (ArrayList<Object> next : nexts)
                {
                    MnForm.sql.setIdTask((Integer) next.get(0));
                    //получили дату начала последующей задачи
                    Date news = (Date) MnForm.sql.selectValue(72).get(0).get(1);
                    //получаем самую первую последующую задачу
                    if (news.before(begNext))
                        begNext = news;
                }
            }

            if(statusTask==-1)
            {
                begNext = dateWork.addDays((Date)curTask.get(2),-1);
            }
            if(statusTask==1)
            {
                endPrev = dateWork.addDays((Date)curTask.get(1),1);
            }

            plusButton.setDisable(false);
            minusButton.setDisable(false);

            if(dateWork.addDays(newDate,1).compareTo(begNext)==0)
            {
                plusButton.setDisable(true);
            }
            else if(dateWork.addDays(newDate,-1).compareTo(endPrev)==0)
            {
                minusButton.setDisable(true);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    //обновление в таблице sql
    private void changeDays(int where)
    {
        //посмотреть на предыдущие задачи
        try
        {
            //устанавливаю имя задачи
            MnForm.sql.setTypeTask(nameTask);
            //id следующей задачи
            ArrayList<Object> curTask = MnForm.sql.selectValue(73).get(0);

            //в рамках полученных двух дат можно перемещать
            //если статус
            ArrayList<Object> row = new ArrayList<Object>();
            //если статус = -1 то перемещается начало
            if(statusTask==-1)
            {
                //дату окончания ставится 1 день перед окончанием текущей задачи
                // в этих рамках задачу можно перемещать
                //дата которая перемещается
                Date oldDate = (Date)curTask.get(1);
                //если where==-1 то перемещение влево
                //если where==1 то перемещение вправо

                Date newDate = dateWork.addDays(oldDate,where);
                row.add(newDate);

                MnForm.sql.changeValue(row, 71);
            }
            else
                //если статус == 1 то перемещается конец
                if(statusTask==1)
                {
                    //дату начала ставится 1 день после начала текущей задачи
                    // в этих рамках задачу можно перемещать
                    //дата которая перемещается
                    Date oldDate = (Date)curTask.get(2);

                    //убавляется день
                    Date newDate = dateWork.addDays(oldDate,where);

                    row.add(newDate);
                    MnForm.sql.changeValue(row, 72);
                }


        }
        catch (SQLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        MnForm.updTaskTable();
    }


    //обновление в графике
    private void newDate(int where)
    {
        //1) - очистка данных графика
        SData.clear();
        lineChart.getData().clear();

        ArrayList<Date> dates = currentGraph.get(new cString(nameTask));
        Date dateBeg = dates.get(0);
        Date dateEnd = dates.get(1);

        Date nD = null;
        //для currentGraph
        if(statusTask==-1)
        {
            nD = dateWork.addDays(dateBeg,where);
            nD = dateWork.objectToDate2(nD);
            //установить у текущей задачи дату
            currentGraph.get(new cString(nameTask)).set(0, nD);
            //для всех пред задач устанавливается новые данные
            for (cString entry : currentGraph.keySet())
            {
                //если второе
                if(entry.getParent().equals(nameTask))
                {
                    currentGraph.get(entry).set(1, nD);
                }
            }
            //для всех последующих изменить
            //дату начала текущего - 2 элемент в массиве
            changeNext(nameTask, 2, nD, currentGraph);
        }
        else
        if(statusTask==1)
        {
            nD = dateWork.addDays(dateEnd,where);
            nD = dateWork.objectToDate2(nD);
            //установить у текущей даты
            currentGraph.get(new cString(nameTask)).set(2, nD);
            //для конечного - для всех
            //последующих изменять дату начала - 0 элемент в массиве
            changeNext(nameTask, 0, nD, currentGraph);
        }

        //для dataGraph
        if(statusTask==-1)
        {
            dataGraph.get(new cString(nameTask)).set(0, nD);
            //поменять данные в предыдущих задачах
            for (cString entry : currentGraph.keySet())
            {
                //если второе
                if(entry.getParent().equals(nameTask))
                {
                    currentGraph.get(entry).set(1, nD);
                }
            }
            //поменять данные в след задачах
            changeNext(nameTask, 2, nD, dataGraph);
        }
        else
        if(statusTask==1)
        {
            currentGraph.get(new cString(nameTask)).set(2, nD);
            //поменять данные в след задачах
            changeNext(nameTask, 0, nD, dataGraph);
        }

        currentGraph = cString.sortingMap(currentGraph);
        addAllValue(currentGraph);
        plusButton.setDisable(true);
        minusButton.setDisable(true);
    }

    //изменить слудующие задачи для изменившейся новой
    private void changeNext(String task, int index, Date nD,
                            Map<cString, ArrayList<Date>> data)
    {
        for (cString entry : data.keySet())
        {
            //если это соединение и предыдущая задача = текущей
            if(entry.checkRoot()&&entry.getChild().equals(task))
            {
                data.get(entry).set(index, nD);
            }
        }
    }







    //установить поле для zoom
    private void setUpZooming(final Rectangle rect,
                              final Node zoomingNode)
    {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<Point2D>();
        zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                rect.setWidth(0);
                rect.setHeight(0);
            }
        });

        zoomingNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                double x = event.getX();
                double y = event.getY();
                rect.setX(Math.min(x, mouseAnchor.get().getX()));
                rect.setY(Math.min(y, mouseAnchor.get().getY()));
                rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
                rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
            }
        });
    }

    //приближение
    private void doZoom(Rectangle zoomRect, LineChart<Date, String> chart)
    {

        double Tgap = 0;
        double Tgap2 = 0;

        double axisShift, xaxisShift;
        Date xnewLowerBound, xnewUpperBound;

        //получение координат квадрата увеличения
        rectinitY.set(zoomRect.getY());
        rectY.set(zoomRect.getY() + zoomRect.getHeight());
        rectinitX.set(zoomRect.getX());
        rectX.set(zoomRect.getX() + zoomRect.getWidth());


        //количество дней на оси координат
        int allDate = dateWork.daysBetween(xAxis.getLowerBound(),xAxis.getUpperBound());

        //получаем ось Y
        CategoryAxis yAxis = (CategoryAxis) lineChart.getYAxis();
        //получаем ось Х
        DateAxis xAxis = (DateAxis) lineChart.getXAxis();


        //получение координат
        ObservableList<String> cats = yAxis.getCategories();
        oldcats = cats;

        //величина в пикселях между метками на оси координат Y
        Tgap = yAxis.getHeight()/cats.size();

        //смещение на фоне оси координат Y
        axisShift = getSceneShiftY(yAxis);

        //величина в пикселях между метками на оси координат X
        Tgap2 = xAxis.getWidth()/allDate;

        //смещение на фоне оси координат X
        xaxisShift = getSceneShiftX(xAxis);


        //индексы,которые нужно установить у оси координат Y
        int yInit = (int) ((rectinitY.get() - axisShift) / Tgap);
        int yEnd =  (int) (( rectY.get() - axisShift) / Tgap);

        int ch;
        //перестановка, если не квадрат увеличения наоборот
        if(yInit>yEnd)
        {
            ch = yEnd;
            yEnd = yInit;
            yInit = ch;
        }
        //размер
        int size = cats.size();

        //новый список
        ArrayList<String> newCats = new ArrayList<String>();

        for(int z=size-yEnd-1;z<size-yInit-1;z++)
        {
            if(z<0)z=0;
            if(z>cats.size()-1) break;
            newCats.add(cats.get(z));
        }

        //новый радиус
        yAxis.invalidateRange(newCats);
        yAxis.setAutoRanging(false);

        //значения на оси X
        int firstValue =  (int) ((rectinitX.get() - xaxisShift) / Tgap2);
        int lastValue  =  (int) ((rectX.get() - xaxisShift) / Tgap2);

        //соответствующее добавление дней на ось X
        xnewLowerBound = dateWork.addDays(xAxis.getLowerBound(), firstValue);
        xnewUpperBound = dateWork.addDays(xAxis.getLowerBound(), lastValue);

        //не восстанавливать
        //yAxis.setAutoRanging(false);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(xnewLowerBound);
        xAxis.setUpperBound(xnewUpperBound);
        xAxis.setTickLabelGap(1);


        //список дат
        ArrayList<Date> dateList = new ArrayList<Date>();
        dateList.clear();

        //сдедать список дат
        while(!xnewLowerBound.equals(xnewUpperBound))
        {
            dateList.add(xnewLowerBound);
            xnewLowerBound = dateWork.addDays(xnewLowerBound,1);
        }


        //на ось Х
        xAxis.invalidateRange(dateList);

        zoomRect.setWidth(0);
        zoomRect.setHeight(0);
    }

    //относительное смещение для оси координат
    private static double getSceneShiftX(Node node)
    {
        double shift = 0;
        do {
            shift += node.getLayoutX();
            node = node.getParent();
        } while (node != null);
        return shift;
    }

    //относительное смещение для оси координат
    private static double getSceneShiftY(Node node)
    {
        double shift = 0;
        do
        {
            shift += node.getLayoutY();
            node = node.getParent();
        }
        while (node != null);
        return shift;
    }

}


