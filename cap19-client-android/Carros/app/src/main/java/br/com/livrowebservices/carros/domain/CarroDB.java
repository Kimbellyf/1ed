package br.com.livrowebservices.carros.domain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CarroDB extends SQLiteOpenHelper {
    private static final String TAG = "sql";

    // Nome do banco
    private static final String NOME_BANCO = "livro_android.sqlite";
    private static final int VERSAO_BANCO = 1;

    public CarroDB(Context context) {
        // context, nome do banco, factory, versão
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Criando a Tabela carro...");
        db.execSQL("create table if not exists carro (_id integer primary key,nome text, desc text, url_foto text,url_info text,url_video text, latitude text,longitude text, tipo text);");
        Log.d(TAG, "Tabela carro criada com sucesso.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Caso mude a versão do banco de dados, podemos executar um SQL aqui
        if (oldVersion == 1 && newVersion == 2) {
            // Execute o script para atualizar a versão...
        }
    }

    // Insere um novo carro, ou atualiza se já existe.
    // O id do carro vem do web service.
    // Vai salvar ou atualizar no SQLite com o mesmo id.
    public long save(Carro carro) {
        long id = carro.id == null? 0 : carro.id;
        SQLiteDatabase db = getWritableDatabase();
        try {

            ContentValues values = getContentValues(carro, id);

            // insert into carro values (...)
            id = db.insert("carro", "", values);
            return id;
        } finally {
            db.close();
        }
    }

    // Atualiza o carro
    public long update(Carro carro) {
        long id = carro.id == null? 0 : carro.id;
        SQLiteDatabase db = getWritableDatabase();
        try {

            ContentValues values = getContentValues(carro, id);

            // Verifica se o carro existe
            Cursor cursorExists = db.query("carro", null, "_id = " + id , null, null, null, null);
            boolean exists = cursorExists.getCount() > 0;

            String _id = String.valueOf(carro.id);
            String[] whereArgs = new String[]{_id};

            // update carro set values = ... where _id=?
            int count = db.update("carro", values, "_id=?", whereArgs);

            return count;
        } finally {
            db.close();
        }
    }

    @NonNull
    private ContentValues getContentValues(Carro carro, long id) {
        ContentValues values = new ContentValues();
        values.put("_id", id);
        values.put("nome", carro.nome);
        values.put("desc", carro.desc);
        values.put("url_foto", carro.urlFoto);
        values.put("url_info", carro.urlInfo);
        values.put("url_video", carro.urlVideo);
        values.put("latitude", carro.latitude);
        values.put("longitude", carro.longitude);
        values.put("tipo", carro.tipo);
        return values;
    }

    // Deleta o carro
    public int delete(Carro carro) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // delete from carro where _id=?
            int count = db.delete("carro", "_id=?", new String[]{String.valueOf(carro.id)});
            Log.i(TAG, "Deletou [" + count + "] registro.");
            return count;
        } finally {
            db.close();
        }
    }

    // Deleta os carros do tipo fornecido
    public int deleteCarrosByTipo(String tipo) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // delete from carro where tipo=?
            int count = db.delete("carro", "tipo=?", new String[]{tipo});
            Log.i(TAG, "Deletou [" + count + "] registros");
            return count;
        } finally {
            db.close();
        }
    }

    // Consulta a lista com todos os carros
    public List<Carro> findAll() {
        SQLiteDatabase db = getReadableDatabase();
        try {
            // select * from carro
            Cursor c = db.query("carro", null, null, null, null, null, null, null);

            return toList(c);
        } finally {
            db.close();
        }
    }

    // Consulta o carro pelo tipo
    public List<Carro> findAllByTipo(String tipo) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            // "select * from carro where tipo=?"
            Cursor c = db.query("carro", null, "tipo = '" + tipo + "'", null, null, null, null);
            return toList(c);
        } finally {
            db.close();
        }
    }

    public int count(Long id) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            // "select * from carro where tipo=?"
            Cursor c = db.query("carro", null, "_id = " + id , null, null, null, null);
            int count = c.getCount();
            return count;
        } finally {
            db.close();
        }
    }

    public Carro getById(Long id) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            // "select * from carro where tipo=?"
            Cursor c = db.query("carro", null, "_id = " + id , null, null, null, null);
            List<Carro> carros = toList(c);
            Carro carro = carros.size() > 0 ? carros.get(0) : null;
            return carro;
        } finally {
            db.close();
        }
    }

    // Lê o cursor e cria a lista de carros
    private List<Carro> toList(Cursor c) {
        List<Carro> carros = new ArrayList<Carro>();

        if (c.moveToFirst()) {
            do {
                Carro carro = new Carro();

                // recupera os atributos de carro
                carro.id = c.getLong(c.getColumnIndex("_id"));
                carro.nome = c.getString(c.getColumnIndex("nome"));
                carro.desc = c.getString(c.getColumnIndex("desc"));
                carro.urlInfo = c.getString(c.getColumnIndex("url_info"));
                carro.urlFoto = c.getString(c.getColumnIndex("url_foto"));
                carro.urlVideo = c.getString(c.getColumnIndex("url_video"));
                carro.latitude = c.getString(c.getColumnIndex("latitude"));
                carro.longitude = c.getString(c.getColumnIndex("longitude"));
                carro.tipo = c.getString(c.getColumnIndex("tipo"));

                carros.add(carro);

            } while (c.moveToNext());
        }

        return carros;
    }

    // Executa um SQL
    public void execSQL(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL(sql);
        } finally {
            db.close();
        }
    }

    // Executa um SQL
    public void execSQL(String sql, Object[] args) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL(sql, args);
        } finally {
            db.close();
        }
    }
}