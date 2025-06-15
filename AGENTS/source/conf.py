# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = 'sim-suite-ai'
copyright = '2025, Alessandro Zappatore'
author = 'Alessandro Zappatore'
release = '1.0'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = [
    'sphinx.ext.autodoc',      # La più importante: importa i moduli e tira fuori la documentazione dai docstring.
    'sphinx.ext.napoleon',     # Permette a Sphinx di capire i docstring in stile Google (che sono molto leggibili).
    'sphinx.ext.viewcode',     # Aggiunge un link "View code" accanto alla documentazione per vedere il codice sorgente.
    'sphinx.ext.todo',         # Permette di usare direttive come .. todo:: per note personali.
]

templates_path = ['_templates']
exclude_patterns = []



# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'alabaster'
html_static_path = ['_static']

autodoc_typehints_format = 'short'

html_theme = 'furo'