# pde_gemfire_tools
License tools for Gemfire

#Included
* **CoreCountFunction**: 
    * _A function that is executed on members that will verify core cound om the machine gemfire is running._
    
    ```
    gfsh> execute function --id=CoreCountFunction --member=<member-id>
    ```