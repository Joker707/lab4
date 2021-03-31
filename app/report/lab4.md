# Цели

- Ознакомиться с принципами работы adapter-based views.
- Получить практические навыки разработки адаптеров для view

## Задача 1. Знакомство с библиотекой (unit test)

1. Метод strictModeThrowsException()

По сути, код не сильно чем-то отличается от NormalMode:
Мы просто у цикла изменяем предел итерирования на `cfg.maxValid - 1` и после этого берем нашу нулевую запись
Но там уже обрабатывается исключение, потому что у нас не Normal mode, а Strict, и наша нулевая запись больше не валидна

__Листинг 1.1 - strictMode__

        @Test
          public void strictModeThrowsException() throws IOException {
            BibDatabase database = openDatabase("/references.bib");
            BibConfig cfg = database.getCfg();
            cfg.strict = true;
        
            BibEntry first = database.getEntry(0);
            for (int i = 0; i < cfg.maxValid - 1; i++) {
              BibEntry unused = database.getEntry(0);
              assertNotNull("Should not throw any exception @" + i, first.getType());
            }
        
            try {
              BibEntry unused = database.getEntry(0);
              first.getType();
            } catch (IllegalStateException e) {
                System.out.println("ISE: " + e.getMessage());
            }
          }

2. Метод shuffleFlag()

Для этого метода я создал ещё один .bib файл, который назвывается очень символично `shuffleFlag.bib`
У первой записи в нем тип `INPROCEEDINGS`, у второй `MISC`, и у третьей `UNPUBLISHED`, после этого идут Артиклей
И дальше мы просто проверяем, если хотя бы одна из первых трех записей стоит не на своем месте, то записи перемешались
Конечно, здесь может произойти такая ситуация, когда этот тест не пройдет, но этот шанс черезвычайно мал!

__Листинг 1.2 - shuffleFlag__

        @Test
          public void shuffleFlag() throws IOException {
            boolean result = false;
            BibDatabase database = openDatabase("/shuffleFlag.bib");
            if ((database.getEntry(0).getType() != Types.INPROCEEDINGS) ||
                    (database.getEntry(1).getType() != Types.MISC) ||
                    (database.getEntry(2).getType() != Types.UNPUBLISHED)) {
              result = true;
            }
            assertTrue(result);
          }


После этого мы собрали наш `biblib` через `./gradlew build`,
получили .JAR файл,и импортнули его в наш проект для следущего задания!
Вроде, несложно, но почему-то последний шаг занял у меня немного больше времени, чем написание тех двух тестов :)

## Задача 2 - Знакомство с RecyclerView.


Ниже представлены наши xml файлы: Один с RecyclerView, второй содержит "шаблон" для заполнения входных данных
(короче, как должны представляться наши записи из базы данных на экране, вот!)

__Листинг 2.1 - activity_main.xml__

    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">
    
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
    </LinearLayout>
    


__Листинг 2.2 - entries.xml__

    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_height="wrap_content"
        android:layout_width="match_parent"
        android:orientation="vertical"
        android:padding="10dp"
        android:background="@color/colorPrimaryDark">
    
        <TextView
            android:id="@+id/title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textColor="@color/white"
            android:textSize="20sp"
            />
    
        <TextView
            android:id="@+id/author"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textColor="@color/white"
            android:textSize="15sp"
            />
    
        <TextView
            android:id="@+id/pages"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textColor="@color/white"
            android:textSize="12sp"
            />
    
        <TextView
            android:id="@+id/year"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textColor="@color/white"
            android:textSize="12sp"
            />
    
        <Space
            android:layout_width="match_parent"
            android:layout_height="5dp"
            android:background="@color/white"/>
    
    
    </LinearLayout>


Перейдем уже к самому коду, его здесь не особо много!

__Листинг 2.3 - Adapter.kt__

    class Adapter(base: InputStream) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        private val database = BibDatabase(InputStreamReader(base))
    
        class ViewHolder(binding: EntriesBinding) : RecyclerView.ViewHolder(binding.root) {
            val author = binding.author
            val title = binding.title
            val year = binding.year
            val pages = binding.pages
        }
    
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = EntriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }
    
        override fun getItemCount(): Int {
            return database.size()
        }
    
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = database.getEntry(position)
            holder.author.text = entry.getField(Keys.AUTHOR)
            holder.pages.text = "Pages: " + entry.getField(Keys.PAGES)
            holder.title.text = entry.getField(Keys.TITLE)
            holder.year.text = entry.getField(Keys.YEAR)
        }
    }
    
Итак!

- На вход у нас подается наша база данных `articles.bib`
- Наследуемся от `RecyclerView.Adapter<Adapter.ViewHolder>()`, передаваемый тип это наш внутренний класс для биндинга полей;
- Метод `onCreateViewHolder` возвращает новый ViewHolder с параметром `binding`(он же `EntriesBinding`),
который мы используем в следующем методе
- В методе `onBindViewHolder` мы наполняем наши текстовые поля
- Метод `getItemCount`  отвечает за кол-во отображаемых записей


__Листинг 2.4 - MainActivity.kt__

    class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
    
            val binding = ActivityMainBinding.inflate(layoutInflater)
    
            val manager = LinearLayoutManager(this)
    
            binding.recyclerView.apply {
                addItemDecoration(DividerItemDecoration(context, manager.orientation))
                layoutManager = manager
                adapter = Adapter(resources.openRawResource(R.raw.articles))
            }
    
            setContentView(binding.root)
        }
    }
    
Разбираемся:
- Биндим наш основной ActivityMain Layout.
- Создаем обычный вертикальный LinearLayout с помощью менеджера
- Вызываем  __apply__ для recyclerView.
 Добавляем ItemDecoration через Divider с двумя параметрами(контекст и вертикальная ориентация в нашем случае)
 И ещё менеджер с адаптером, которые уже были у нас описаны!




## Задача 3 - Бесконечный список.

Для того, чтобы выполнить это задание нам нужно всего лишь изменить две строчки:
- Первая в `onBindViewHolder`

    `val entry = database.getEntry(position % database.size())`
    
- И вторая в `getItemCount`

    `override fun getItemCount(): Int {
            return Int.MAX_VALUE
        }`
    

# Выводы

Итого, на лабораторную работу потратил порядка 10 часов
Большую часть времени разбирался по теории, что и как работает
Первое задание было достаточно несложным
Основную массу времени заняло второе(думаю, тут ничего удивителнього :))
Ну и третье задание тоже было такое, как контрольный вопрос!

В ходе выполнения познакомился с такой большой и тяжелой штукой, как RecyclerView,
которая может в одинчестве обрабатывать большые базы данных
А вот если Вам такое не нужно, то лучше и не заморачивать себе им голову
Для каких-то очень маленьких объемов данных, которые вполне могут уместиться в размер вашего дисплея,
отлично подойдут те же самые ListView или даже LinearLayout! :)
