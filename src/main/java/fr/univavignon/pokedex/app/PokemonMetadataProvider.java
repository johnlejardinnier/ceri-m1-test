package fr.univavignon.pokedex.app;

import com.google.gson.Gson;
import fr.univavignon.pokedex.api.IPokemonMetadataProvider;
import fr.univavignon.pokedex.api.PokedexException;
import fr.univavignon.pokedex.api.PokemonMetadata;
import fr.univavignon.pokedex.tools.Curl;
import fr.univavignon.pokedex.tools.IGSerializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by jonathan on 05/04/17.
 */
public class PokemonMetadataProvider implements IPokemonMetadataProvider, IGSerializer {

    private String API = "http://hoomies.fr/pokemeta/?id=";

    //private String rootPath;

    private String path;

    public PokemonMetadataProvider() {
        //this.rootPath = System.getProperty("user.home") + "/";
        this.path = ".pokedex42/data/pokemons/";
        this.initPath(path);
    }

    @Override
    public PokemonMetadata getPokemonMetadata(int index) throws PokedexException {

        if (index <= 0 || index > 150) {
            throw new PokedexException("Id is not valid !");
        }

        String link = API + index;

        PokemonMetadata metadata = null;

        try {

            if (checkFile(path, Integer.toString(index))) {
                System.out.println("Loading metadata: " + index);
                metadata = (PokemonMetadata) this.loadData(Integer.toString(index));
            } else {
                System.out.println("Downloading metadata: " + index);
//                String content = this.curl(link);
                String content = Curl.curl(link);
                metadata = this.json2PokemonMetadata(content);
                this.saveData(metadata);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return metadata;
    }


    /**
     * Create PokemonMetadata from Json
     *
     * @param content
     * @return pokemonMetadata
     */
    private PokemonMetadata json2PokemonMetadata(String content) {

        Gson g = new Gson();

        PokemonMetadata pokemonMetadata = g.fromJson(content, PokemonMetadata.class);

        return pokemonMetadata;
    }

    @Override
    public void saveData(Object object) throws IOException, PokedexException {

        PokemonMetadata pokemonMetadata = (PokemonMetadata) object;

        if (pokemonMetadata == null) {
            throw new PokedexException("Couldn't save empty metadata !");
        }

        if (path == null) {
            throw new PokedexException("Metadata path is not defined !");
        }

        String filename = this.getFileName(path, Integer.toString(pokemonMetadata.getIndex()));


        this.persistData(filename, pokemonMetadata);
    }

    @Override
    public Object loadData(String name) throws FileNotFoundException, PokedexException {

        if (path == null) {
            throw new PokedexException("Metadata path is not defined !");
        }

        PokemonMetadata pokemonMetadata = null;

        String filename = this.getFileName(path, name);

        try (Reader reader = new FileReader(filename)) {

            Gson gson = new Gson();

            pokemonMetadata = gson.fromJson(reader, PokemonMetadata.class);

            if (pokemonMetadata == null) {
                throw new PokedexException("Error loading Metadata !");
            }

            reader.close();
        } catch (IOException e) {
            e.getMessage();
        }


        return pokemonMetadata;
    }


}
